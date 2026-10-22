# Protótipo 3D do TC Quantum

Fonte do artefato "TC Quantum Modelos" (visualizador three.js de todos os blocos, com estados, paletas e animações).
Os modelos JSON de `mods/quantum/.../models/block/` saem daqui.

## Estrutura

- `parts/00-head.html` — página, estilos e ajudas comuns (caixas, materiais, texturas).
- `parts/NN-*.js` — um arquivo por modelo (ou família). `01-nucleo...` também tem as paletas (`CORE_PALETTES`) e a base
  de contenção partilhada (`coreBase`, usada pelo Núcleo e pelo Regulador).
- `parts/19-cena.js` — cena, interface e o gancho `window.__tcq` usado pelos scripts.
- `build.py` — junta `parts/*` em `tc-quantum-modelos.html` (abre direto no navegador).

## Uso

```bash
cd tools/prototype
python3 build.py                                  # gera tc-quantum-modelos.html

# scripts em Node (render e exportação) precisam de um Chromium:
mkdir pw && cd pw && npm init -y && npm i playwright-core three@0.128.0 @sparticuz/chromium && cd ..

node sheet.js regulador 0,1,3 /tmp/reg.png       # folha de contacto: modelo, estados, imagem [câmara] [paleta]
node export_models.js out                         # exporta as peças paradas de todos os modelos para out/*.json
node export_models.js out nucleo regulador        # só alguns
```

Depois de exportar, copie os JSON para `models/block/` e rode, na raiz do repo:

```bash
python3 tools/textures/quantum_textures.py   # gera as texturas glow_/glass_/flat_ das cores novas
python3 tools/textures/remap_uv.py           # acerta as UVs pela posição
```

(Para uma cor nova aparecer, crie antes um PNG qualquer com o nome `glow_<cor>.png` na pasta de texturas; o script
redesenha-o.)

## Como o exportador decide

- **Peça parada** (vai para o JSON) = visível e no mesmo sítio em todos os estados e instantes amostrados. O que se
  mexe ou some (braços, cristais, tesserato, raios, itens) fica para o BlockEntityRenderer.
- Rotações: o Minecraft só aceita um eixo por peça e ângulos de 22,5° em 22,5°. O exportador procura o ângulo exato e
  arredonda (avisa quando arredonda mais de 0,6°). Na exportação os anéis redondos usam 16 segmentos para cair certo.
- O ângulo em Y vai com o sinal trocado em relação ao three.js (convenção do Minecraft, a mesma dos modelos antigos).
- Cores: as luzes saem com a paleta padrão (**Magenta + Roxo**), em texturas `glow_<cor>`.
- Deslocamento por modelo (`MAP` no topo de `export_models.js`): os 3×3×3 têm o bloco principal no centro da camada do
  meio (y −16); os 1×2 e o Terminal têm o principal em baixo.
