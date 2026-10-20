#!/usr/bin/env python3
"""
Gera as texturas de bloco "de material" do TC Quantum (16x16 px), sem borda e emendáveis (o lado direito continua o
esquerdo e o de cima continua o de baixo). Os modelos pegam cada face pela posição no espaço (ver remap_uv.py), então
a textura precisa emendar sem costura.

Uso (na raiz do workspace; precisa do Pillow: pip install pillow):
    python3 tools/textures/quantum_textures.py

Para mudar uma cor, edite MATERIALS abaixo e rode de novo. As texturas "glow_<cor>", "glass_<cor>" e "flat_<cor>"
são geradas a partir do próprio nome do arquivo que já existe na pasta. Texturas com desenho próprio (vent, keys,
monitor_*) não são tocadas.
"""
import os
import random
import zlib

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OUT = os.path.join(ROOT, "mods/quantum/src/main/resources/assets/tcquantum/textures/block")
SIZE = 16

# nome: (cor base, variação do ruído, riscos horizontais de metal escovado)
MATERIALS = {
    "metal": ("#68717F", 11, True),
    "frame": ("#3F4754", 9, False),
    "dark": ("#262B34", 7, False),
    "panel": ("#2E3542", 8, False),
    "trim": ("#F97316", 13, False),
}


def rgb(hex_color):
    h = hex_color.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def clamp(v):
    return max(0, min(255, int(round(v))))


def seamless_noise(seed, amplitude):
    """Ruído suave que emenda nas bordas: valores aleatórios borrados com vizinhança que dá a volta (wrap)."""
    rnd = random.Random(seed)
    raw = [[rnd.uniform(-1, 1) for _ in range(SIZE)] for _ in range(SIZE)]
    out = [[0.0] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            total = 0.0
            for dy in (-1, 0, 1):
                for dx in (-1, 0, 1):
                    weight = 2.0 if dx == 0 and dy == 0 else 1.0
                    total += raw[(y + dy) % SIZE][(x + dx) % SIZE] * weight
            # metade suave (manchas) + metade pixel a pixel (o grão do estilo do Minecraft)
            out[y][x] = (total / 10.0 * 2.2 * 0.55 + raw[y][x] * 0.6) * amplitude
    return out


def material(name, base, amplitude, brushed):
    r, g, b = rgb(base)
    noise = seamless_noise(zlib.crc32(name.encode()), amplitude)
    rnd = random.Random(name)
    streaks = {rnd.randrange(SIZE): rnd.uniform(-4, 5) for _ in range(5)} if brushed else {}
    im = Image.new("RGBA", (SIZE, SIZE))
    px = im.load()
    for y in range(SIZE):
        for x in range(SIZE):
            d = noise[y][x] + streaks.get(y, 0)
            px[x, y] = (clamp(r + d), clamp(g + d), clamp(b + d), 255)
    return im


def glow(hex_color):
    """Luz: cor uniforme com variação mínima (peças pequenas mostram a cor certa, não uma borda)."""
    r, g, b = rgb(hex_color)
    noise = seamless_noise(int(hex_color, 16) & 0xFFFF, 3)
    im = Image.new("RGBA", (SIZE, SIZE))
    px = im.load()
    for y in range(SIZE):
        for x in range(SIZE):
            d = noise[y][x]
            px[x, y] = (clamp(r + d), clamp(g + d), clamp(b + d), 255)
    return im


def glass(hex_color):
    """Vidro: tinta translúcida uniforme com um reflexo diagonal fino (o reflexo também emenda)."""
    r, g, b = rgb(hex_color)
    im = Image.new("RGBA", (SIZE, SIZE))
    px = im.load()
    for y in range(SIZE):
        for x in range(SIZE):
            sheen = (x + y) % SIZE in (5, 6)
            if sheen:
                px[x, y] = (clamp(r + 50), clamp(g + 40), clamp(b + 30), 110)
            else:
                px[x, y] = (r, g, b, 64)
    return im


def main():
    for name, (base, amplitude, brushed) in MATERIALS.items():
        material(name, base, amplitude, brushed).save(os.path.join(OUT, name + ".png"))
    for file in sorted(os.listdir(OUT)):
        stem = file[:-4]
        if stem.startswith("glow_"):
            glow(stem[5:]).save(os.path.join(OUT, file))
        elif stem.startswith("glass_"):
            glass(stem[6:]).save(os.path.join(OUT, file))
        elif stem.startswith("flat_"):
            material(stem, "#" + stem[5:], 8, False).save(os.path.join(OUT, file))
    print("texturas geradas em", OUT)


if __name__ == "__main__":
    main()
