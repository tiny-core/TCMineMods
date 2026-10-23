# TC Quantum — instruções específicas

As regras gerais (autor, idioma, arquitetura, segurança, desempenho, documentação, git) estão no
`CLAUDE.md` da raiz do workspace. Aqui, só o que é deste mod.

Armazenamento e automação **sem cabos**: um Núcleo Quântico obrigatório e blocos "entrelaçados" com ele. Plano
completo, telas e modelos: `docs/PLANO.md`. **Ler o plano antes de mudar regras de
`storage/` ou `grid/`.**

Estado: **fase 1, fatia 1.4** — Núcleo Quântico (cubo 3×3×3, base 3×3, contorno/colisão em `block/core/CoreShape`), redes gravadas no mundo (`QuantumNetworks`), energia FE com espera, Entrelaçador, Matriz de Superposição (Itens) com **Módulos de Qubits I–V** (5 encaixes), e o **conteúdo da rede** (pools de itens e fluidos) gravado no mundo. **Efeitos V2** do Núcleo (`client/render/core/`, `CoreAnimation`/`CoreCrystal`/`CoreRings`/`CoreBeam`/`CoreArms`/`CoreBolts`) e da Matriz (`client/render/matrix/`, `MatrixSpine`/`MatrixCells`). Texturas remapeadas (UV por posição, materiais emendáveis). **Modelos v2** (2026-10-21) no estilo do Núcleo, exportados de `tools/prototype/`. Próxima: 1.5 Terminal (aba Rede).

## Decisões de design em vigor (detalhe em `docs/DECISOES.md`)

- Linguagem visual de todos os blocos = a do Núcleo: base baixa de metal, filetes de luz, figura de luz central,
  braços flutuantes; visual limpo, nada atravessa os braços; "Sem energia" apaga e some com o que é animado;
  surgimento ao ligar. Estado visual vem do servidor, nunca é decidido no render.
- **Cor por rede (opção A)**: o dono escolhe a paleta no Núcleo e todos os blocos seguem; padrão **Magenta + Roxo**
  (6 paletas em `CORE_PALETTES`, `tools/prototype/parts/01-*`). Hoje as cores estão nas texturas `glow_*`; a cor
  por rede vai precisar de tint (BlockColor) ou de cor no BER. Depende da fatia 1.6 (dono da rede).
- **Regulador de Estoque 3×3×3, um por base**, com muitas regras; mesma base do Núcleo; tesserato (hipercubo 4D) com
  o item dentro, arestas acendem com o estoque e brilham na altura do alvo; 4 braços.
- **Bateria e Processador: um por rede, 3×3×3**, upgrades (teto: `Amounts.MAX` de energia; canais presos ao
  `TickBudget`). **Túnel: um por dimensão, obrigatório em todas** (inclusive a do Núcleo), 3×3×3, sem braços.
- **Matriz: várias por rede, 1×2**, com filtros (item/tag/mod) e prioridade (filtradas primeiro).
- Colapsador 3×3×3; Terminal 3×1×2 (principal embaixo no meio). Blocos 3×3×3 partilham a base de contenção.
- **Interface** = porta da rede (importa/exporta itens, fluidos, gás, energia; redstone com filtros; expõe a rede; AE2 via
  `MEStorage` em `integration/ae2`; recusa rede espelhada; uma classe por função). Emissor de Nível mantido.
- Em aberto: na Matriz, **o módulo define o tipo** (recomendado) ou um bloco por tipo. Perguntar antes de mexer.

## Próximos passos (ordem sugerida)

1. Refazer a fatia 1.5 (Terminal, aba Rede).
2. ~~BERs novos do Núcleo e da Matriz para os modelos v2~~ **feito** — luzes da base sem energia (blockstate apagado), auréola do Núcleo (precisa textura), conteúdo fluido/gás da Matriz.
3. Ainda no protótipo: Interface, Painel da Interface, Âncoras, Entrelaçador, Monitor, Painel da Rede, Portátil.

## Regras que não podem ser quebradas

- Quantidades sempre por `Amounts` (saturação em `Amounts.MAX`); nunca somar `long` de quantidade direto.
- Limites do jogo: `Amounts.MAX` (maior `long` do Java) é o teto do pool. Toda passagem do pool para uma API do
  Minecraft ou de outro mod que usa `int` (pilhas, `IItemHandler`, `FluidStack` em mB, `IEnergyStorage`) passa por
  `Amounts.toInt` e respeita o tamanho máximo da pilha. Tabela completa no plano, §2.4.
- O conteúdo da rede vive no **pool** (`ResourcePool`), nunca nos blocos. Matriz só soma capacidade.
- Mover recursos: sempre `simulate = true` antes de `false`; o que não couber volta para a origem.
- Nada de trabalho pesado no tick sem `TickBudget`; blocos que trabalham sozinhos usam `Backoff`.
- Config é `COMMON`, nunca `SERVER`.
- A rede (energia, conteúdo) vive em `QuantumNetworks` (`SavedData`); o bloco do Núcleo guarda só o **id**, que vai
  para o item ao quebrar (`copy_components` na loot table). Nunca guardar conteúdo da rede num block entity.
- Núcleo: só o **centro** tem block entity; os 26 `CoreStructureBlock` acham o centro pelas propriedades
  ox/oy/oz (`CoreFootprint`). Só o centro derruba item; estrutura nunca tem loot.

- Bloco que entra na rede: block entity herda `block/EntangledBlockEntity` e o bloco implementa `block/EntangledBlock`.
  A rede (`NetworkMembers`) é a fonte da verdade de capacidade e ligações; o bloco guarda só o id da rede.
- Item e fluido do disco: leitura **estrita** (`ItemKey.parse`/`FluidKey.parse` com `CODEC.parse(...).result()`);
  qualquer erro = ilegível, e o registro fica **órfão** em `NetworkStorage`, gravado de volta intacto. Nunca usar o
  `ItemStack.parse` comum (aceita resultado parcial) nem apagar o que não deu para ler.
- Carregar do disco usa `ResourcePool.restore` (ignora a capacidade); o jogo usa `insert`/`extract`.
- Efeitos de bloco (`client/render/`): só desenham, nunca decidem. O estado vem do bloco (Núcleo: `ONLINE`) ou do
  block entity sincronizado pelo servidor só quando muda, no máximo 1×/s (Matriz: `visualOnline`, `visualFill`).
  As posições dos efeitos da Matriz copiam o modelo do autor: mudou câmara/medidor/LED/anel no Blockbench, atualizar as
  constantes do `MatrixSpine` e `MatrixCells`.
- Partículas e tudo que é visual ficam em `client/` (`@EventBusSubscriber(value = Dist.CLIENT)`); o servidor decide o
  que mostrar e manda por pacote (`network/`).

## Pacotes

| Pacote | O quê |
|---|---|
| `storage/` | `Amounts` (aritmética saturada), `ResourcePool` (recurso → quantidade, capacidade, mudanças para sync), `ItemKey`/`FluidKey` (identidade do recurso, sem quantidade), `QuantityFormat` (1.2k, 3.4M ... 9.2E) |
| `grid/` | `QuantumNetworks` (`SavedData` com todas as redes) sobre `NetworkDirectory` (qual rede um Núcleo assume, Núcleo mais próximo), `NetworkRecord`, `NetworkPower` (energia e espera), `NetworkMembers`/`NetworkMember`/`MemberKind` (blocos entrelaçados), `NetworkStorage` (pools e órfãos), `BlockLocation`; `TickBudget`, `Backoff` |
| `block/` | `EntangledBlock` + `EntangledBlockEntity`: base de todo bloco que entra numa rede |
| `block/matrix/` | `SuperpositionMatrixBlock` (1×2), o block entity, `MatrixModules` (encaixes e capacidade), `MatrixInteractions` (cliques e mensagens), `GaugeLevels` (regras dos medidores) |
| `item/` | `EntanglerItem`, `QubitModuleItem` |
| `network/` | `ModNetwork`, `EntanglementLinesPayload`, `EntanglementBroadcaster` (manda as ligações 1×/s) |
| `client/` | `ClientSetup` (registra os renderizadores), `ClientPayloadHandler`, `EntanglementLines` (partículas) |
| `client/render/` | `EffectBoxes`, `EffectPalette`, `AppearClock` (base compartilhada); `core/` (`CoreEffectsRenderer`, `CoreAnimation`, `CoreCrystal`, `CoreRings`, `CoreBeam`, `CoreArms`, `CoreBolts`); `matrix/` (`MatrixEffectsRenderer`, `MatrixSpine`, `MatrixCells`) |
| `block/core/` | `QuantumCoreBlock` (centro), `CoreStructureBlock` (os 26 em volta), `QuantumCoreBlockEntity`, `CoreEnergyStorage` (FE), `QuantumCoreItem` (coloca com o clicado como base), `CoreFootprint` (geometria), `CoreShape` (contorno e colisão) |
| `registry/` | Blocos, itens, block entities, componentes de item, aba do criativo, capabilities |

## Modelos e texturas

Em `src/main/resources/assets/tcquantum/` (`models/block`, `models/item`, `textures/block`), exportados do protótipo
e ajustados pelo autor no Blockbench: **não regerar nem reescrever** esses arquivos sem pedido dele. Mapa arquivo →
bloco, bloco principal dos multiblocos e peças do BlockEntityRenderer: `docs/MODELOS.md`. UV por posição no espaço
(`tools/textures/remap_uv.py`, só mexe nos `"uv"`) e texturas de material emendáveis
(`tools/textures/quantum_textures.py`); textura nova de material = sem borda, para emendar.
O protótipo 3D e o exportador dos JSON estão em `tools/prototype/` (README lá): peças paradas → JSON, o que se
mexe → BlockEntityRenderer. Paleta padrão das luzes: **Magenta + Roxo**.

## Comandos (na raiz do workspace)

```bash
./gradlew :quantum:build             # jar em build/libs/ da raiz (instale junto com o do core)
./gradlew :quantum:test --rerun
./gradlew :quantum:runClient
```
