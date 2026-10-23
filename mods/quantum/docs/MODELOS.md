# Modelos e texturas — TC Quantum

Os modelos em `src/main/resources/assets/tcquantum/models/` e as texturas em `textures/block/` foram **gerados do
protótipo 3D** aprovado na fase 0 (artefato "TC Quantum Modelos"). São JSON normais de modelo do Minecraft: abra no
**Blockbench** (Arquivo → Abrir modelo → "Java Block/Item") e ajuste à vontade; o código só depende dos **nomes dos
arquivos**.

## O que está em JSON e o que não está

Cada modelo traz só as peças **paradas**. As peças que se mexem (esfera e anéis do Núcleo, partículas, coluna de
energia, feixes, hologramas, o item a flutuar no Regulador...) **não** estão no JSON: são desenhadas pelo
BlockEntityRenderer de cada bloco, com as mesmas cores, quando o bloco for implementado. As luzes (texturas `glow_*`)
brilham no escuro: no build, toda parte com textura `glow_*` ganha `"neoforge_data": {"block_light": 15, "sky_light": 15}`
(`build.gradle` do mod). O campo **não** fica nos arquivos daqui porque o Blockbench o apaga ao salvar; para uma
parte brilhar, basta usar uma textura cujo nome começa com `glow_`.

## Arquivos

| Arquivo | Bloco | Tamanho | Bloco principal | Peças animadas (BER) |
|---|---|---|---|---|
| `block/quantum_core.json` | Núcleo Quântico | 3×3×3 | o do **centro** (camada do meio) | braços flutuantes, cristal de voxels, anéis, coluna de energia, raios (base de contenção partilhada) |
| `block/terminal.json` | Terminal | 3×1×2 | o de **baixo no meio** | hologramas, feixes, teclas, cubo de dados |
| `block/superposition_matrix.json` | Matriz de Superposição | 1×2 (várias por rede) | o de **baixo** | células, fantasmas, conteúdo, pulsos, medidores, coroa |
| `block/vacuum_battery.json` | Bateria de Vácuo | 3×3×3 (uma por rede) | o do **centro** (camada do meio) | plasma do toro, pontos quentes, pulsos do solenoide, braços, raios (as bobinas e o solenoide são fixos) |
| `block/quantum_processor.json` | Processador Quântico | 3×3×3 (um por rede) | o do **centro** (camada do meio) | candelabro, cabos, chip, qubits, pulsos de frio, 4 braços curtos, raios |
| `block/quantum_tunnel.json` | Túnel Quântico | 3×3×3 (um por dimensão) | o do **centro** (camada do meio) | anéis do mastro, pulsos, par entrelaçado, orbes de dimensão, feixe curto, ondas (sem braços) |
| `block/collapser.json` | Colapsador | 3×3×3 | o do **centro** (camada do meio) | buraco negro, disco, braços, itens, partículas, raios |
| `block/quantum_interface.json` | Interface Quântica (bloco) | 1×1 | — | cubo de dados 4×4×4 (voxels neutros que acendem na cor dos canais ativos), voxels a entrar e sair pelas faces, plano de leitura |
| `block/interface_panel.json` | Interface (painel) | painel na face | — | cubo de dados 3×3×3, voxels entre o cubo e o bloco, plano de leitura |
| `block/machine_anchor.json` | Âncora de Máquina | chip na face | — | ícone 3D (brilho) |
| `block/inventory_anchor.json` | Âncora de Inventário | chip na face | — | — |
| `block/level_emitter.json` | Emissor de Nível | chip na face | — | — |
| `block/quantum_manipulator.json` | Manipulador Quântico | 1×1 | — | íris, lente, cristal, garras, alvo, feixe, partículas, setas |
| `block/stock_regulator.json` | Regulador de Estoque | 3×3×3 (um por base) | o do **centro** (camada do meio) | tesserato, item, braços, raios (base de contenção partilhada) |
| `block/monitor.json`, `block/network_panel.json` | Monitor, Painel da Rede | 1×1 (Painel: multibloco) | — | o ecrã inteiro da frente |
| `item/entangler.json` | Entrelaçador (item) | — | — | orbe e par entrelaçado |
| `item/portable_terminal.json` | Terminal Portátil (item) | — | — | holograma, orbe |
| `item/quantum_core.json` | Núcleo Quântico (item) | — | — | usa o modelo do bloco reduzido (`display`); ajuste a escala aqui se ficar grande ou pequeno |
| `block/core_structure.json` | Blocos de estrutura do Núcleo | — | — | vazio de propósito (só a textura das partículas ao quebrar) |
| `block/superposition_matrix_upper.json` | Metade de cima da Matriz | — | — | vazio de propósito (o modelo inteiro sai da metade de baixo) |
| `item/superposition_matrix.json` | Matriz (item) | — | — | modelo do bloco reduzido (`display`) |

**Modelos de 2026-10-21 (estilo do Núcleo):** gerados pelo exportador de `tools/prototype/` (ver o README de lá), com a paleta padrão **Magenta + Roxo**. Os blocos 3×3×3 (Núcleo, Regulador, Bateria, Processador, Túnel) usam a **mesma base de contenção**, que ocupa o chão 3×3 inteiro (~48 px em XZ, 15,6 px de altura). A Interface é uma moldura aberta e simétrica; o Painel é uma face dessa moldura.

**Efeitos V2 (novos, 2026-10-23):**

- **Núcleo** (`client/render/core/`): `CoreEffectsRenderer` (monta o quadro), `CoreAnimation` (surgimento em 2,6 s), `CoreCrystal` (cristal octaédrico de 184 voxels + miolo), `CoreRings` (2 anéis), `CoreBeam` (coluna + faíscas), `CoreArms` (4 braços; animados, apagados sem energia), `CoreBolts` (8 raios, sem estado). Sem auréola: precisa de textura. Raio dos braços: 20 px (animado, sai do 3×3).

- **Matriz** (`client/render/matrix/`): `MatrixEffectsRenderer`, `MatrixSpine` (linha de dados, 2 medidores de 10 segmentos, luz de estado, pulsos, anel da coroa — pintados por cima das luzes do JSON), `MatrixCells` (5 células com vidro, arestas, fantasma e conteúdo de itens; fluido e gás quando houver módulos desses tipos). Surgimento em 2,4 s.

- **Compartilhados** (`client/render/`): `AppearClock` (relógio do surgimento + tempo ligado que pausa), `EffectPalette` (Magenta + Roxo), `EffectBoxes` (público: modo luz, modo metal sombreado, barra fina "beam").

As posições dos efeitos da Matriz copiam o modelo: mudou câmara/medidor/LED/anel no Blockbench, atualizar as constantes do `MatrixSpine` e `MatrixCells`.

A **frente** dos modelos de bloco com orientação (Matriz) é o lado **sul** (z = 16) do modelo; o blockstate gira a
partir daí. O `item/entangler.json` (e o `item/portable_terminal.json`) ainda não tem `display`: no Blockbench, aba
*Display*, ajuste o tamanho no inventário e na mão.

Chips e painéis (Âncoras, Emissor, Interface em painel) estão modelados **colados na face norte** do próprio bloco
(z = 0); o blockstate roda para as outras 5 faces. Blocos maiores que 1 usam blocos de estrutura invisíveis com
colisão (plano, §6.1); um modelo de bloco só pode ir de −16 a 32 px em cada eixo, por isso o "bloco principal" é o
indicado acima.

## Texturas e UV

**UV por posição no espaço:** cada face pega a textura no lugar onde está no modelo (escala 1:1), em vez de sempre o
canto (0, 0). Peças vizinhas continuam o desenho umas das outras e peças finas não mostram só a borda. Quem faz isso
é `tools/textures/remap_uv.py`; depois de criar peças novas no Blockbench, rode-o de novo (só mexe nos números de
`"uv"`; geometria e formatação ficam iguais). `vent`, `keys` e `monitor_*` têm desenho próprio e não são remapeadas.

**Texturas de material** (`metal`, `frame`, `dark`, `panel`, `trim`, todas as `glow_*`, `glass_*` e `flat_*`): sem
borda e emendáveis, geradas por `tools/textures/quantum_textures.py` (cores na tabela `MATERIALS` do script). As
luzes são de cor uniforme, para peças pequenas mostrarem a cor da luz e não uma moldura.


Itens em `textures/item/`: `qubit_module_1` … `qubit_module_5` (chip com qubit roxo e um ponto por nível, na cor do
nível). São provisórias, geradas por script: redesenhe à vontade mantendo o nome.

Blocos em `textures/block/`:

16×16 px, no desenho do protótipo: `metal`, `frame`, `dark`, `panel`, `trim` (laranja da marca), `vent`, `keys`,
`monitor_*`; `glow_<cor>` (luzes), `glass_<cor>` (vidro translúcido; os modelos com vidro usam
`"render_type": "minecraft:translucent"`), `flat_<cor>` (madeira e ouro). Pode redesenhar qualquer uma no Blockbench
ou noutro editor mantendo o nome.
