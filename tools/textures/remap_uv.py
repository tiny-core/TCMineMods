#!/usr/bin/env python3
"""
Remapeia as UVs dos modelos do TC Quantum pela posição de cada face no espaço ("projeção de mundo").

Antes: toda face pegava a textura a partir do canto (0, 0), então peças pequenas mostravam sempre o mesmo pedaço (e
peças finas, só a primeira linha). Agora cada face pega a textura no lugar onde está no modelo: peças vizinhas
continuam o desenho umas das outras, como se a textura estivesse pintada no espaço. Funciona porque as texturas de
material emendam sem costura (quantum_textures.py).

Regras:
  - escala 1:1 (1 px de textura por px de modelo), igual a antes;
  - faces maiores que 16 px usam a textura inteira (0 a 16);
  - texturas com desenho próprio (vent, keys, monitor_*) ficam como estão;
  - só os números de "uv" mudam; geometria, rotação e formatação do arquivo ficam iguais.

Uso (na raiz do workspace):
    python3 tools/textures/remap_uv.py            # todos os modelos com peças
    python3 tools/textures/remap_uv.py terminal   # só um (nome do arquivo, sem .json)
"""
import glob
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
MODELS = os.path.join(ROOT, "mods/quantum/src/main/resources/assets/tcquantum/models")
KEEP = ("/vent", "/keys", "/monitor_")
UV = re.compile(r'"uv"\s*:\s*\[[^\]]*\]')


def span(start, size):
    """Intervalo de textura (0 a 16) para uma face que começa em `start` com `size` px."""
    if size >= 16:
        return 0.0, 16.0
    u0 = start % 16
    if u0 + size > 16:
        u0 = 16 - size
    return u0, u0 + size


def face_uv(face, frm, to):
    x0, y0, z0 = frm
    x1, y1, z1 = to
    w, h, d = x1 - x0, y1 - y0, z1 - z0
    if face in ("north", "south"):
        u = span(16 - x1, w) if face == "north" else span(x0, w)
        v = span(16 - y1, h)
    elif face in ("east", "west"):
        u = span(16 - z1, d) if face == "east" else span(z0, d)
        v = span(16 - y1, h)
    else:  # up / down
        u = span(x0, w)
        v = span(z0, d) if face == "up" else span(16 - z1, d)
    return [u[0], v[0], u[1], v[1]]


def number(value):
    text = f"{value:.3f}".rstrip("0").rstrip(".")
    return text if text not in ("-0", "") else "0"


def remap(path):
    text = open(path, encoding="utf-8").read()
    model = json.loads(text)
    elements = model.get("elements")
    if not elements:
        return 0
    textures = model.get("textures", {})
    new_uvs = []
    for element in elements:
        for face, data in element.get("faces", {}).items():
            if "uv" not in data:
                continue
            ref = data.get("texture", "")
            name = textures.get(ref[1:], ref) if ref.startswith("#") else ref
            if any(k in name for k in KEEP):
                new_uvs.append(None)  # mantém
            else:
                new_uvs.append(face_uv(face, element["from"], element["to"]))
    matches = list(UV.finditer(text))
    if len(matches) != len(new_uvs):
        raise SystemExit(f"{path}: {len(matches)} 'uv' no texto, {len(new_uvs)} faces; nada alterado")
    out, last, changed = [], 0, 0
    for match, uv in zip(matches, new_uvs):
        out.append(text[last:match.start()])
        if uv is None:
            out.append(match.group(0))
        else:
            out.append('"uv": [' + ", ".join(number(v) for v in uv) + "]")
            changed += 1
        last = match.end()
    out.append(text[last:])
    result = "".join(out)
    json.loads(result)  # garante JSON válido antes de gravar
    open(path, "w", encoding="utf-8").write(result)
    return changed


def main():
    only = set(sys.argv[1:])
    total = 0
    for path in sorted(glob.glob(os.path.join(MODELS, "*", "*.json"))):
        if only and os.path.basename(path)[:-5] not in only:
            continue
        changed = remap(path)
        if changed:
            print(f"{os.path.relpath(path, ROOT)}: {changed} faces")
            total += changed
    print("total:", total, "faces remapeadas")


if __name__ == "__main__":
    main()
