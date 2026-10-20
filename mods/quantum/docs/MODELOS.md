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
| `block/quantum_core.json` | Núcleo Quântico | 3×3×3 | o do **centro** (camada do meio) | esfera, anéis, qubits, feixes, orbes e auréola |
| `block/terminal.json` | Terminal | 2×1×2 | o de **baixo à esquerda** (olhando de frente) | holograma |
| `block/superposition_matrix.json` | Matriz de Superposição | 1×2 | o de **baixo** | partículas das câmaras, medidores |
| `block/vacuum_battery.json` | Bateria de Vácuo | 1×2 | o de baixo | coluna de energia, espiral, arco |
| `block/quantum_processor.json` | Processador Quântico | 1×2 | o de baixo | pulso de frio, qubits |
| `block/quantum_tunnel.json` | Túnel Quântico | 1×2 | o de baixo | orbe, par entrelaçado, feixe curto |
| `block/collapser.json` | Colapsador | 2×2×2 | o de **baixo**, no canto (a definir na fatia do bloco) | buraco negro, disco, feixes, partículas |
| `block/quantum_interface.json` | Interface Quântica (bloco) | 1×1 | — | cristal, anel da face ativa |
| `block/interface_panel.json` | Interface (painel) | painel na face | — | nenhuma (setas por textura animada) |
| `block/machine_anchor.json` | Âncora de Máquina | chip na face | — | ícone 3D (brilho) |
| `block/inventory_anchor.json` | Âncora de Inventário | chip na face | — | — |
| `block/level_emitter.json` | Emissor de Nível | chip na face | — | — |
| `block/quantum_manipulator.json` | Manipulador Quântico | 1×1 | — | alvo, feixe, partículas, setas |
| `block/stock_regulator.json` | Regulador de Estoque | 1×1 | — | coluna, anéis de pulso, item a flutuar |
| `block/monitor.json`, `block/network_panel.json` | Monitor, Painel da Rede | 1×1 (Painel: multibloco) | — | o ecrã inteiro da frente |
| `item/entangler.json` | Entrelaçador (item) | — | — | orbe e par entrelaçado |
| `item/portable_terminal.json` | Terminal Portátil (item) | — | — | holograma, orbe |
| `item/quantum_core.json` | Núcleo Quântico (item) | — | — | usa o modelo do bloco reduzido (`display`); ajuste a escala aqui se ficar grande ou pequeno |
| `block/core_structure.json` | Blocos de estrutura do Núcleo | — | — | vazio de propósito (só a textura das partículas ao quebrar) |
| `block/superposition_matrix_upper.json` | Metade de cima da Matriz | — | — | vazio de propósito (o modelo inteiro sai da metade de baixo) |
| `item/superposition_matrix.json` | Matriz (item) | — | — | modelo do bloco reduzido (`display`) |

A **frente** dos modelos de bloco com orientação (Matriz) é o lado **sul** (z = 16) do modelo; o blockstate gira a
partir daí. O `item/entangler.json` (e o `item/portable_terminal.json`) ainda não tem `display`: no Blockbench, aba
*Display*, ajuste o tamanho no inventário e na mão.

Chips e painéis (Âncoras, Emissor, Interface em painel) estão modelados **colados na face norte** do próprio bloco
(z = 0); o blockstate roda para as outras 5 faces. Blocos maiores que 1 usam blocos de estrutura invisíveis com
colisão (plano, §6.1); um modelo de bloco só pode ir de −16 a 32 px em cada eixo, por isso o "bloco principal" é o
indicado acima.

## Texturas

16×16 px, no desenho do protótipo: `metal`, `frame`, `dark`, `panel`, `trim` (laranja da marca), `vent`, `keys`,
`monitor_*`; `glow_<cor>` (luzes), `glass_<cor>` (vidro translúcido; os modelos com vidro usam
`"render_type": "minecraft:translucent"`), `flat_<cor>` (madeira e ouro). Pode redesenhar qualquer uma no Blockbench
ou noutro editor mantendo o nome.
