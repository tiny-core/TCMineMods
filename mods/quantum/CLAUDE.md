# TC Quantum — instruções específicas

As regras gerais (autor, idioma, arquitetura, segurança, desempenho, documentação, git) estão no
`CLAUDE.md` da raiz do workspace. Aqui, só o que é deste mod.

Armazenamento e automação **sem cabos**: um Núcleo Quântico obrigatório e blocos "entrelaçados" com ele. Plano
completo, telas e modelos: `docs/planos/tc-quantum.md` (raiz do workspace). **Ler o plano antes de mudar regras de
`storage/` ou `grid/`.**

Estado: **fase 1, fatia 1.1** — módulo, config e o domínio puro (pool de recursos, aritmética saturada, orçamento
de tick, espera com recuo). Nada aparece em jogo ainda além do mod na lista.

## Regras que não podem ser quebradas

- Quantidades sempre por `Amounts` (saturação em `Amounts.MAX`); nunca somar `long` de quantidade direto.
- Limites do jogo: `Amounts.MAX` (maior `long` do Java) é o teto do pool. Toda passagem do pool para uma API do
  Minecraft ou de outro mod que usa `int` (pilhas, `IItemHandler`, `FluidStack` em mB, `IEnergyStorage`) passa por
  `Amounts.toInt` e respeita o tamanho máximo da pilha. Tabela completa no plano, §2.4.
- O conteúdo da rede vive no **pool** (`ResourcePool`), nunca nos blocos. Matriz só soma capacidade.
- Mover recursos: sempre `simulate = true` antes de `false`; o que não couber volta para a origem.
- Nada de trabalho pesado no tick sem `TickBudget`; blocos que trabalham sozinhos usam `Backoff`.
- Config é `COMMON`, nunca `SERVER`.

## Pacotes

| Pacote | O quê |
|---|---|
| `storage/` | `Amounts` (aritmética saturada), `ResourcePool` (recurso → quantidade, capacidade, mudanças para sync) |
| `grid/` | `TickBudget` (tempo por tick da rede), `Backoff` (espera 1, 2, 4... ticks quando ocioso) |

## Comandos (na raiz do workspace)

```bash
./gradlew :quantum:build             # jar em build/libs/ da raiz (instale junto com o do core)
./gradlew :quantum:test --rerun
./gradlew :quantum:runClient
```
