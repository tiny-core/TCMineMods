# TC Quantum — instruções específicas

As regras gerais (autor, idioma, arquitetura, segurança, desempenho, documentação, git) estão no
`CLAUDE.md` da raiz do workspace. Aqui, só o que é deste mod.

Armazenamento e automação **sem cabos**: um Núcleo Quântico obrigatório e blocos "entrelaçados" com ele. Plano
completo, telas e modelos: `docs/PLANO.md`. **Ler o plano antes de mudar regras de
`storage/` ou `grid/`.**

Estado: **fase 1, fatia 1.2** — o Núcleo Quântico (cubo 3×3×3 com blocos de estrutura), as redes gravadas no mundo
(`QuantumNetworks`), energia FE com espera sem energia, e a aba do criativo. Próximas: 1.3 Entrelaçador, 1.4 Matriz.

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

## Pacotes

| Pacote | O quê |
|---|---|
| `storage/` | `Amounts` (aritmética saturada), `ResourcePool` (recurso → quantidade, capacidade, mudanças para sync), `QuantityFormat` (1.2k, 3.4M ... 9.2E) |
| `grid/` | `QuantumNetworks` (`SavedData` com todas as redes) sobre `NetworkDirectory` (regra de qual rede um Núcleo assume), `NetworkRecord`, `NetworkPower` (energia e espera), `CoreLocation`; `TickBudget`, `Backoff` |
| `block/core/` | `QuantumCoreBlock` (centro), `CoreStructureBlock` (os 26 em volta), `QuantumCoreBlockEntity`, `CoreEnergyStorage` (FE), `QuantumCoreItem` (coloca com o clicado como base), `CoreFootprint` (geometria) |
| `registry/` | Blocos, itens, block entities, componentes de item, aba do criativo, capabilities |

## Modelos e texturas

Em `src/main/resources/assets/tcquantum/` (`models/block`, `models/item`, `textures/block`), exportados do protótipo
e ajustados pelo autor no Blockbench: **não regerar nem reescrever** esses arquivos sem pedido dele. Mapa arquivo →
bloco, bloco principal dos multiblocos e peças do BlockEntityRenderer: `docs/MODELOS.md`.

## Comandos (na raiz do workspace)

```bash
./gradlew :quantum:build             # jar em build/libs/ da raiz (instale junto com o do core)
./gradlew :quantum:test --rerun
./gradlew :quantum:runClient
```
