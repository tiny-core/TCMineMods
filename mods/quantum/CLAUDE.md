# TC Quantum — instruções específicas

As regras gerais (autor, idioma, arquitetura, segurança, desempenho, documentação, git) estão no
`CLAUDE.md` da raiz do workspace. Aqui, só o que é deste mod.

Armazenamento e automação **sem cabos**: um Núcleo Quântico obrigatório e blocos "entrelaçados" com ele. Plano
completo, telas e modelos: `docs/PLANO.md`. **Ler o plano antes de mudar regras de
`storage/` ou `grid/`.**

Estado: **fase 1, fatia 1.4** — Núcleo Quântico (cubo 3×3×3), redes gravadas no mundo (`QuantumNetworks`), energia FE
com espera, Entrelaçador, Matriz de Superposição (Itens) com **Módulos de Qubits I–V**, e o **conteúdo da rede**
(pools de itens e fluidos) gravado no mundo, e os **efeitos** (V1) do Núcleo e da Matriz. Texturas remapeadas
(UV por posição, materiais emendáveis). Próxima: 1.5 Terminal (aba Rede).

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
  As posições dos efeitos da Matriz copiam o modelo do autor: mudou câmara/medidor/LED no Blockbench, atualizar as
  constantes do `MatrixEffectsRenderer`.
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
| `client/render/` | `EffectBoxes` (caixas coloridas com luz máxima), `CoreEffectsRenderer`, `MatrixEffectsRenderer` |
| `block/core/` | `QuantumCoreBlock` (centro), `CoreStructureBlock` (os 26 em volta), `QuantumCoreBlockEntity`, `CoreEnergyStorage` (FE), `QuantumCoreItem` (coloca com o clicado como base), `CoreFootprint` (geometria) |
| `registry/` | Blocos, itens, block entities, componentes de item, aba do criativo, capabilities |

## Modelos e texturas

Em `src/main/resources/assets/tcquantum/` (`models/block`, `models/item`, `textures/block`), exportados do protótipo
e ajustados pelo autor no Blockbench: **não regerar nem reescrever** esses arquivos sem pedido dele. Mapa arquivo →
bloco, bloco principal dos multiblocos e peças do BlockEntityRenderer: `docs/MODELOS.md`. UV por posição no espaço
(`tools/textures/remap_uv.py`, só mexe nos `"uv"`) e texturas de material emendáveis
(`tools/textures/quantum_textures.py`); textura nova de material = sem borda, para emendar.

## Comandos (na raiz do workspace)

```bash
./gradlew :quantum:build             # jar em build/libs/ da raiz (instale junto com o do core)
./gradlew :quantum:test --rerun
./gradlew :quantum:runClient
```
