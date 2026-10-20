# Plano — TC Quantum (`tcquantum`)

> Estado: **fase 1 em curso** — 1.1 (módulo, config, domínio puro), 1.2 (Núcleo Quântico, redes no mundo, energia) e 1.3 (Entrelaçador,
> Matriz de Itens somando capacidade) e 1.4 (Módulos de Qubits, conteúdo da rede no mundo) e V1 (efeitos do Núcleo e da
> Matriz) feitas, texturas remapeadas; próxima: 1.5 Terminal (aba Rede). Telas e modelos fechados na fase 0.
>
> Revisão 2: tema de física quântica; números gigantes; Links completos (energia, redstone, fluidos,
> químicos); máquinas detectadas sozinhas (slots, saídas com chance, multiblocos); Bateria; SCC como
> referência.

Mod NeoForge 1.21.1 de armazenamento e automação **sem cabos**, no espírito do AE2 e do SCC (Super
Cool Calculator). Tudo gira em torno de um **Núcleo Quântico**: sem ele carregado, a rede não existe.
Os blocos não precisam de cabos porque estão **entrelaçados** com o Núcleo.

Diferenciais: o **Observatório** (hierarquia do que dá para fabricar agora, sem repetidos), **Links
inteligentes** que entendem a máquina sozinhos, e **performance** como requisito, não como extra.

---

## 1. Princípios (valem mais que qualquer detalhe abaixo)

1. **Performance primeiro, sempre mensurável.** Todo trabalho periódico corre num orçamento de
   tempo por tick (config, padrão 2 ms por rede). O que não cabe passa para o tick seguinte.
2. **Sem limite de jogo.** Nada de canais, tipos por disco, itens/segundo por bloco ou tiers de
   velocidade. Uma operação move a quantidade inteira de uma vez; o único limite real de
   velocidade é o que a máquina de destino aceita.
3. **Números gigantes, sem perder desempenho** (§2.4): contagens em `long` com saturação — até
   9,2 quintilhões por tipo de recurso.
4. **Na dúvida, perder uma operação é melhor que duplicar.** Toda extração é `SIMULATE` antes de
   `EXECUTE`; o que não couber volta para a origem.
5. **Os dados vivem no Núcleo, não nos blocos.** O conteúdo da rede fica num `SavedData` da rede.
6. **Compatibilidade por capacidades padrão** (`IItemHandler`, `IFluidHandler`, `IEnergyStorage`,
   capacidades de químicos do Mekanism), nunca por mixin ou reflection em outros mods.
7. **Cada mod externo num pacote `integration/<mod>/`.**
8. **O cliente só pede.** Todo pacote do cliente é validado no servidor.

## 2. A rede

```
Núcleo Quântico (1 por rede, identidade = UUID da rede)
 ├─ Pools por tipo (Itens, Fluidos, Químicos, Energia) — capacidade = soma das Matrizes/Baterias
 ├─ Livro de receitas da rede (aprendidas, lidas e padrões manuais)
 ├─ Processadores Quânticos (crafts em paralelo)
 └─ Blocos entrelaçados (sem cabo): Terminais, Interfaces, Âncoras (Links), Matrizes, Baterias...
```

### 2.1 Entrelaçamento (como um bloco entra na rede)

O **TC Entrelaçador** (item) clica no Núcleo para guardar o "estado" da rede e depois nos blocos para
os entrelaçar. Colocar um bloco perto do Núcleo (raio na config) entrelaça sozinho.

- **Alcance:** padrão mesma dimensão, sem limite de distância; o **TC Túnel Quântico** liga outras
  dimensões.
- **Chunk descarregado:** o armazenamento continua a contar; máquinas e baús descarregados são
  ignorados até voltarem. Nunca carregamos chunks.
- **Ver as ligações:** com o Entrelaçador na mão, linhas finas de partículas ligam cada bloco ao
  Núcleo, com cor por tipo de bloco.

### 2.2 Armazenamento em pool por tipo

Cada tipo de recurso tem **um pool só por rede** (mapa `recurso → long`); as Matrizes e Baterias
só somam **capacidade**. Inserir e extrair é O(1), sem procurar disco por prioridade.

| | AE2 (células) | TC Quantum (pool) |
|---|---|---|
| Inserir/extrair | percorre células por prioridade | O(1) |
| Armazenamento num chunk descarregado | os itens somem da rede | continuam lá |
| Partição por disco | sim | não (filtros nas Interfaces e na Reserva cobrem os casos reais) |
| Partir um bloco de armazenamento | itens vão no item da célula | a capacidade diminui; acima do limite, a rede só aceita retiradas. Nada é destruído |

Partir o Núcleo dá um item "Núcleo entrelaçado" que guarda o UUID; recolocá-lo restaura tudo.

### 2.3 Tipos de recurso (extensível)

Uma abstração `ResourceType` define como ler, guardar e mover cada tipo. Novo tipo = adaptador novo.

| Tipo | Fonte | Fase |
|---|---|---|
| Itens | vanilla/NeoForge | 1 |
| Fluidos | NeoForge | 1 |
| Energia (FE) — **Bateria** | NeoForge | 2 |
| Químicos (gases, infusão, pigmento, pasta, **resíduo nuclear**) | Mekanism (`Chemical` na 1.21.1) | 6 |
| Fonte (Ars Nouveau), Mana (Botania)... | adaptadores futuros | depois |

**Radioativos:** só entram na Matriz Química com o upgrade **Blindagem de Chumbo**. Partir uma
Matriz com radioativo quando o pool ficaria acima da capacidade é bloqueado. Config para desligar.

### 2.4 Números gigantes, com velocidade

O Integrated Dynamics mostra números enormes; o que importa para nós é **guardar e somar sem custo**.

| Opção | Limite | Custo |
|---|---|---|
| `int` (AE2 em alguns pontos, inventários vanilla) | 2,1 mil milhões | — |
| **`long` com saturação (escolha)** | 9,2 quintilhões por recurso (≈ 144 mil biliões de pilhas) | igual ao `int`, operação nativa da CPU |
| `BigInteger` | ilimitado | objeto novo por soma: 10–50× mais lento e cria lixo para o GC — exatamente o que causa lag |

**Saturação** = em vez de dar a volta para negativo quando passa do máximo, a conta para no máximo
(o que não cabe volta à origem). Na tela os números aparecem compactos (1,2 Qi, 340 T) com o valor
exato no tooltip. Totais da rede inteira (soma de todos os tipos) podem passar de 9,2 Qi e por isso
usam `double` só para exibição.

**Limites do Java e do Minecraft (fronteiras).** 9,2 quintilhões é o maior número inteiro que o Java guarda
num `long`; não há como passar disso sem `BigInteger`, por isso é o teto do pool (saturação). Fora do pool, o
Minecraft e os outros mods usam limites menores, e a rede **nunca** os ultrapassa:

| Onde | Tipo | Limite | Como a rede lida |
|---|---|---|---|
| Pilha de itens (`ItemStack`), inventários e canos (`IItemHandler`) | `int` + tamanho da pilha | 2 147 483 647, e no máximo 99 por pilha (64 na maioria dos itens) | entrega em pilhas do tamanho certo, várias por operação |
| Fluidos (`FluidStack`, `IFluidHandler`) | `int` em mB | 2 147 483 647 mB por operação | divide em partes de até `Integer.MAX_VALUE` |
| Energia (`IEnergyStorage`) | `int` em FE | 2 147 483 647 FE por chamada | divide por tick; o buffer da rede é `long` |
| Químicos do Mekanism | `long` | 9,2 quintilhões | direto |
| Comparador / redstone | 0–15 | — | proporcional (Emissor de Nível) |
| NBT e pacotes de rede | `long` (LongTag, VarLong) | 9,2 quintilhões | direto |

Regra no código: toda passagem do pool para uma API do jogo ou de outro mod usa `Amounts.toInt`, que corta
no máximo do `int`, e nunca assume mais do que a API aceita.

## 3. Motor de crafts e o Observatório

### 3.1 De onde vêm as receitas (correção da revisão 1)

Na revisão 1 escrevi que não havia forma universal de ler receitas de máquinas. O SCC mostra que dá,
combinando fontes. Proposta em camadas, da mais confiável para a mais genérica:

| # | Fonte | Como | Confiança |
|---|---|---|---|
| 1 | **Adaptadores por mod** (vanilla, Mekanism, Create, Thermal, Industrial Foregoing, Ars...) | lê o tipo de receita com a API do mod: entradas, saídas, **chances**, energia, tempo | total |
| 2 | **Leitura genérica do `RecipeManager`** | `getIngredients()` / `getResultItem()` de qualquer receita | boa, quando o mod implementa bem esses métodos |
| 3 | **JEI/EMI como guia** | o cliente diz "esta receita X serve para a máquina Y"; o servidor só aceita se a receita X existir no seu `RecipeManager` e relê tudo dele (o cliente nunca define ingredientes) | boa, sem confiar no cliente |
| 4 | **Aprendizado por observação** (§3.4) | a Âncora regista o que entra e o que sai de verdade | cresce com o uso |
| 5 | **Padrão manual** | o jogador codifica, como no AE2 | total (o jogador decidiu) |

Uma receita de máquina só aparece no Observatório se houver **uma máquina capaz entrelaçada**.

### 3.2 O Observatório: hierarquia sem repetidos

- **Camada 0 — Na rede**; **Camada 1 — Direto**; **Camadas 2..N — Em cadeia**.
- **Cada item aparece uma vez só, na camada mais rasa** em que pode ser feito (busca em largura).
- **Quantidade máxima** calculada só para o item selecionado, com teto de tempo.
- Saídas com chance **nunca contam** para "dá para fazer" (ver §3.4); aparecem como "bônus provável".
- Profundidade máxima por config (padrão 6).

Performance: índice invertido `ingrediente → receitas`, recalcula só quando um recurso passa de
"tem" para "não tem" (ou o contrário), em thread de trabalho sobre snapshot, só com o Observatório
aberto, diferenças em lotes para o cliente.

### 3.3 Execução

Pedido → plano → reserva no pool → **Processadores Quânticos** (1 craft paralelo cada). Crafts de mesa
o próprio Núcleo faz. Falta de ingrediente a meio: o craft fica "aguardando" com motivo visível.

**Escolha da receita pelo rendimento.** Quando um item tem várias receitas, o plano escolhe a que dá **mais do item
pedido pelo mesmo custo**. Exemplo: 1 tora → 4 tábuas na mesa, 1 tora → 6 tábuas na Serra do Mekanism: o plano usa a
Serra. Regras:

- **Só receitas disponíveis agora**: máquina ligada por Âncora, com energia e não parada (falha, sem saída). Se a
  Serra estiver desligada ou cheia, a mesa entra no lugar, sem o jogador fazer nada.
- **Mesma entrada**: compara a quantidade do item pedido por craft (6 > 4). **Entradas diferentes**: compara o custo
  em matéria-prima, somado pela árvore do Observatório (§3.2), por unidade do item pedido.
- **Saídas com chance não contam** no rendimento (o plano só promete o garantido); contam depois, quando chegam.
- Empate: a receita mais rápida (ops/s medido, §3.5); depois, a que o jogador usou por último.
- Na tela do Pedido: **"Rendimento (padrão) / Rapidez"** — Rapidez troca a ordem (primeiro o tempo, depois o rendimento),
  para quando o jogador quer o item já e tem matéria-prima de sobra.
- Por item, o jogador pode **fixar uma receita** (cadeado na aba Padrões); fixada, o plano não troca.
- Regra pura em `crafting/plan/RecipeChooser`, com testes JUnit (fases 3 e 4); a disponibilidade vem dos grupos de
  máquinas (§3.5).

### 3.4 Âncora de Máquina inteligente (o que o sistema descobre sozinho)

**Ao entrelaçar com uma máquina, a Âncora faz um "reconhecimento":**

1. **Faces e capacidades:** para cada face, que capacidades a máquina expõe (itens, fluidos, energia,
   químicos). Máquinas do Mekanism mudam isto pela configuração de lados delas — a Âncora relê quando
   a máquina avisa mudança.
2. **Slots de entrada e saída:** `insert(SIMULATE)` do ingrediente em cada slot diz quais aceitam
   aquele item (entradas); slots que nunca aceitam nada mas deixam extrair são saídas. Simulação não
   mexe em nada, por isso é seguro.
3. **Receitas da máquina:** pelo ID do bloco e pelo adaptador do mod (fonte 1 de §3.1). O mesmo bloco
   de máquinas diferentes (ex.: Enriquecedor básico e de elite do Mekanism) é reconhecido como o mesmo
   tipo com velocidades diferentes.
4. **Saídas com chance** (ex.: Serra do Mekanism: serragem a 25%): o adaptador lê a chance da receita;
   sem adaptador, a Âncora **aprende por observação** — conta quantas vezes cada saída apareceu em N
   operações e mostra "≈ 24% (120 amostras)".
   - O planejador **nunca conta com saída de chance** para cumprir um pedido; quando ela sai, vai para o
     pool como bônus.
   - Opção por receita: "fazer até obter X da saída secundária" (para quem quer a serragem).
5. **Multiblocos:** quase todos expõem as capacidades por um bloco porta/válvula (Mekanism, Immersive
   Engineering, Create). Âncora na porta = funciona como qualquer máquina. O adaptador do mod diz se a
   estrutura está **formada** e que receitas ela faz (ex.: Evaporação Térmica, SPS). Sem adaptador: o
   jogador associa receitas pelo Observatório/JEI (fonte 3).

**O que a Âncora entrega e recebe** (cada um ligável na tela dela):

| Canal | Para a máquina | Da máquina |
|---|---|---|
| Itens | ingredientes do craft, nos slots certos | resultados e subprodutos |
| Fluidos / Químicos | entradas líquidas e gasosas | saídas líquidas e gasosas |
| Energia | FE da Bateria da rede, **só enquanto há craft** (opcional: sempre, até N FE/t) | — (ou recolher de geradores) |
| Redstone | sinal **ligado durante o craft**, pulso por operação, ou controlado por regra | lê comparador/sinal para saber se a máquina está pronta |

**Inteligência extra:** se a máquina tiver vários slots de entrada iguais (ex.: fábrica de 5 do
Mekanism), a Âncora divide o lote entre eles; se a máquina parar de produzir (falta de energia,
saída cheia), a Âncora marca o motivo e o planejador escolhe outra máquina igual se houver.

### 3.5 Máquinas repetidas: grupos e divisão de carga

A rede deteta sozinha as máquinas iguais e reparte o trabalho entre elas.

- **Grupo automático:** Âncoras cujas máquinas têm a mesma **assinatura** (bloco + tipos de receita
  reconhecidos) formam um grupo, sem o jogador configurar nada. Tiers diferentes da mesma máquina (Enriquecedor
  básico e de elite) ficam no mesmo grupo, com **pesos** diferentes. A aba "Máquina" da Âncora mostra
  "Grupo: Serra de Precisão × 4 (2 elite, 2 básica)".
- **Divisão proporcional:** um pedido de N operações é dividido pelas máquinas livres do grupo na proporção da
  velocidade de cada uma. A velocidade começa pela estimativa do adaptador (tier, melhorias) e passa a ser a
  **medida** (operações por segundo observadas), que acerta sozinha máquinas com melhorias de outros mods.
- **Fábricas e slots paralelos:** uma fábrica de 5 linhas do Mekanism conta como 5 trabalhadores.
- **Limites reais:** cada máquina só recebe o que cabe nas entradas dela (simulação); o resto fica na fila e vai
  para quem libertar espaço primeiro.
- **Lote mínimo** (config): não divide trabalho tão pequeno que o custo de mover seja maior do que o ganho.
- **Máquina parada** (sem energia, saída cheia, multibloco desmontado): peso zero até voltar; o trabalho que
  ainda não foi enviado passa para as outras. O que já está dentro dela fica lá e termina quando ela voltar.
- **Prioridade** (tela da Âncora): decide a ordem quando o trabalho cabe em menos máquinas do que o grupo tem;
  com carga grande, todas trabalham.
- **Código:** regra pura `crafting/exec/LoadBalancer` (recebe pesos, espaços livres e N; devolve quanto vai para
  cada máquina), com testes JUnit; entra na fase 4, junto com a Âncora de Máquina.

## 4. Blocos e itens

### 4.1 Pedidos pelo autor (com nomes do tema)

| # | Nome | Função |
|---|---|---|
| — | **TC Núcleo Quântico** | Sem ele nada funciona. Guarda pools, receitas e a fila de crafts. 2 blocos de altura |
| 1 | **TC Interface Quântica** | Ver, exportar e importar, com filtros e modos automáticos |
| 1b | **TC Interface (painel)** | A mesma Interface em painel fino colado na face de um baú ou máquina, como a parte do AE2. Mesma tela e funções; age só na face onde está; 4 setas mostram o sentido (laranja = exportar, ciano = importar) |
| 2 | **TC Terminal** (abas Rede, Fabricação, Observatório, Padrões) | Grade da rede, mesa 3×3, crafts possíveis |
| 3a | **TC Âncora de Máquina** | Link completo com a máquina (§3.4) |
| 3b | **TC Âncora de Inventário** | Baú/tanque passa a fazer parte da rede |
| 4 | **TC Matriz de Superposição — Itens / Fluidos / Químicos** | Capacidade por tipo, expansível com módulos |
| — | **TC Módulo de Qubits I–V** (item) | Aumenta a capacidade da Matriz (4 encaixes) |
| — | **TC Bateria de Vácuo** | Armazenamento de energia da rede; expansível com módulos; alimenta Âncoras e o Núcleo |
| — | **TC Entrelaçador** (item) | Liga blocos ao Núcleo e mostra as ligações |

### 4.2 Sugestões

| Bloco / item | Para quê | Fase |
|---|---|---|
| **TC Processador Quântico** | +1 craft em paralelo | 3 |
| **TC Codificador** (aba Padrões) | padrões de mesa e de processamento, puxando do JEI/EMI | 3 |
| **TC Regulador de Estoque** | "manter sempre 512 de X": pede crafts sozinho | 5 |
| **TC Emissor de Nível** | redstone quando X passa de/abaixo de N | 5 |
| **TC Terminal Portátil** (item) | terminal e Observatório sem fio | 5 |
| **TC Painel da Rede** | uso por tipo, crafts ativos, energia, **tempo de tick usado** | 5 |
| **Reserva** (aba do Núcleo, não é bloco) | itens que crafts automáticos e o Colapsador nunca usam | 5 |
| **TC Colapsador** | destrói excesso acima de N (tema: colapso da função de onda) | 5 |
| **TC Manipulador Quântico** (ex-Colocador/Quebrador) | retira e coloca **blocos e fluidos** no mundo; melhorias Toque de Seda, Fortuna I–III, Velocidade e Área 3×3 (Seda e Fortuna não juntas); íris laranja = blocos, azul = fluidos; setas laterais para a frente = colocar, para trás = retirar; age como jogador falso do dono (respeita claims) | 5 |
| **TC Monitor** | bloco único que mostra o item escolhido: ícone, quantidade na rede e, por cima, o fluxo (▲ verde a entrar, ▼ vermelho a sair, em /s ou /min; some sem fluxo) | 5 |
| **TC Túnel Quântico** | outras dimensões | 7 |
| **Periférico CC:Tweaked** | API Lua no Núcleo | 7 |
| **Pontes AE2 e MineColonies** | convivência no ATM10 e pedidos das colónias | 7 |
| **Segurança** | dono e permissões por jogador | 1 |
| **Gerador de ponto zero** (opcional, endgame) | fonte de energia própria do mod, cara de fazer | 7 |

### 4.3 TC Quantum frente ao SCC

O SCC (NeoForge 1.21.1, MIT) já faz muito disto: liga baús existentes, lê receitas de máquinas com
JEI/EMI, detecta máquinas pelo ID do bloco, sem padrões. Não faz sentido refazê-lo igual; o que
nos distingue:

| | SCC | TC Quantum |
|---|---|---|
| Armazenamento | os baús que já existem | pool próprio (O(1), números gigantes) **e** Âncoras de Inventário |
| Ver o que dá para fazer | ao pedir um item, mostra a árvore dele | **Observatório**: todas as possibilidades por camada, sem repetidos |
| Link com máquina | itens e módulos | itens, fluidos, químicos, **energia e redstone** no mesmo Link; aprendizado de chances |
| Cabos | usa cabos | **sem cabos** (entrelaçamento) |
| Visual | 27 estilos | identidade TC (cores da marca) + tema quântico |

Por ser MIT, o código do SCC pode servir de **estudo** (como ele lê o JEI, como detecta máquinas)
antes de escrevermos a nossa versão.

## 5. Telas (wireframes)

Todas no design system do core (`ScreenStyle`, `ItemGrid`, `SideToolbar`, `GridHeight`, `TabSlot`) e
nas cores da marca (laranja `#F97316`, ciano `#22B8E8`, fundo escuro).

### 5.1 TC Terminal

```
┌─[Rede][Fabricação][Observatório][Padrões]────────────────────┐
│ 🔍 busca (@mod  #tag)   [Itens|Fluidos|Químicos|Energia] [↕] │ ◧ altura
│ ┌──────────────────────────────────────────────┐ ▲           │ ◧ ordem
│ │ ■ ■ ■ ■ ■ ■ ■ ■ ■   grade da rede (5/8/tela) │ █           │ ◧ só craftáveis
│ └──────────────────────────────────────────────┘ ▼           │
│ ┌ mesa 3×3 ┐   →  [resultado]   [limpar] [↥ devolver à rede] │
│ ▓ inventário do jogador                                      │
└──────────────────────────────────────────────────────────────┘
```

### 5.2 Aba Observatório

```
┌─ Observatório ───────────────────────────────────────────────┐
│ 🔍 busca   [Tudo|Mesa|Máquinas]  Profundidade: [1][2][3][≤6] │
│ ── Direto (37) ───────────────────────────────── ▾ recolher  │
│ ■ ■ ■ ■ ■ ■ ■ ■ ■ ■                                          │
│ ── Em 2 passos (112) ─────────────────────────── ▸ expandir  │
│ ── Em 3+ passos (480) ────────────────────────── ▸ expandir  │
├──────────────────────────────────────────────────────────────┤
│ Selecionado: Circuito Avançado        Máx.: 37   [Pedir ▸]   │
│ árvore: Circuito ◂ Liga Reforçada ◂ Lingote de Ósmio (rede)  │
│                  ◂ Redstone (rede)    via: Enriquecedor ✔    │
│ bônus provável: Pó de Ósmio ≈ 10%                            │
└──────────────────────────────────────────────────────────────┘
```

### 5.3 TC Âncora de Máquina (abas: Máquina | Canais | Receitas | Diagnóstico)

```
┌─[Máquina][Canais][Receitas][Diagnóstico]─────────────────────┐
│ Mekanism · Serra de Precisão (Elite)          Estado: ● livre│
│ Faces:  N[itens→]  S[←itens]  L[energia]  C[—]  B[—]  O[—]   │
│ Slots:  entrada 1 · saída 2 (1 principal, 1 chance)          │
├─ Canais ─────────────────────────────────────────────────────┤
│ [✔] Itens   [✔] Energia: só durante craft  máx [ 20 000 ] FE/t│
│ [ ] Fluidos [ ] Químicos  Redstone: [Durante craft ▾]        │
├─ Receitas (41 reconhecidas · 3 aprendidas) ──────────────────┤
│ Tora de carvalho → 6 Tábuas + Serragem (25%, adaptador)      │
│ Tronco exótico   → 4 Tábuas + Serragem (≈ 24%, 120 amostras) │
└──────────────────────────────────────────────────────────────┘
```

### 5.4 TC Interface Quântica (abas: Conteúdo | Exportar | Importar)

```
┌─[Conteúdo][Exportar][Importar]───────────────────────────────┐
│ Exportar para: ▸ lado [N S L O C B]  Modo: [Manual|Auto]     │
│ Filtro (ghost slots): □ □ □ □ □ □ □ □ □   [Lista branca|negra]│
│ Opções: [Respeitar NBT] [Usar tags] [Manter N na origem]     │
│ Redstone: [Sempre|Com sinal|Sem sinal]   Intervalo: 1 s      │
└──────────────────────────────────────────────────────────────┘
```

A Interface expõe para outros mods **só os slots configurados**, nunca a rede inteira: um
`IItemHandler` com milhões de slots faz os canos de outros mods varrerem tudo a cada tick, a causa
número 1 de lag em redes de armazenamento.

### 5.5 TC Matriz / TC Bateria

```
┌─ TC Matriz de Superposição (Itens) ─────────────┐
│ [Qubits III][Qubits III][Qubits V][   vazio   ]  │
│ Esta matriz: 4,2 M    Rede (itens): 61% de 38 M  │
│ ▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░                              │
└──────────────────────────────────────────────────┘
┌─ TC Bateria de Vácuo ───────────────────────────┐
│ [Módulo][Módulo][Módulo][vazio]                  │
│ Rede: 1,8 T FE de 4 T  · entrada 2,1 M/t · saída 1,4 M/t │
│ gráfico das últimas 60 s (MetricRing do core)    │
└──────────────────────────────────────────────────┘
```

### 5.6 TC Núcleo Quântico

Painel: rede (nome, dono), uso por tipo, energia, processadores ocupados, fila de crafts com motivo
de espera, tempo de tick usado (verde/amarelo/vermelho).

## 6. Modelos 3D (direção visual — tema quântico)

Metal escuro grafite, **linhas de luz ciano** (estado normal) e **acentos laranja** (atividade), as
cores da marca. Elementos quânticos: órbitas de elétrons, esfera de Bloch, partículas que aparecem e
somem (superposição), cristais suspensos. Texturas animadas onde der; itens com 1 px de profundidade.

| Bloco | Forma | Animação |
|---|---|---|
| Núcleo Quântico | 1×2; esfera de energia suspensa no centro, com 3 anéis orbitais de metal à volta, numa base com 4 pilares | esfera pulsa; anéis giram em eixos diferentes; cor por estado (ciano ok, laranja ocupado, vermelho erro) |
| Terminal | bloco com ecrã inclinado | ondas de interferência a correr no ecrã |
| Interface Quântica | cubo de grelha com um anel na face ativa | anel acende no sentido do fluxo |
| Âncora de Máquina / Inventário | placa fina que encosta no bloco, com um pequeno átomo (núcleo + 2 órbitas) | órbitas giram ao transferir; cor do brilho = canal ativo (laranja itens, azul fluidos, verde químicos, amarelo energia, vermelho redstone) |
| Matriz de Superposição (3 tipos) | gabinete com 4 cubos de vidro (um por módulo), cada um com partículas dentro | partículas mais densas quanto mais cheio; faixa de cor do tipo |
| Bateria de Vácuo | cilindro vertical de vidro com espiral de energia, tampas de metal | espiral sobe com a carga |
| Processador Quântico | bloco com chip de qubits visível (grelha de pontos) | pontos acendem por craft ativo |
| Entrelaçador (item) | diapasão com dois cristais ligados por um fio de luz | cristais brilham quando tem rede guardada |

### 6.1 Decisões de modelo (2026-10-20, revisão 3)

Liberdade de tamanho dada pelo autor; só as Âncoras ficam pequenas. Blocos maiores que 1 usam 1 bloco
principal + blocos de estrutura invisíveis com colisão (técnica do Digital Miner do Mekanism): não se
atravessam, colocar exige o espaço livre e partir qualquer parte parte o bloco inteiro.

| Bloco | Tamanho | Nota |
|---|---|---|
| Núcleo Quântico | 3×3×3 | principal = centro do cubo (modelo de bloco vai de −16 a 32 px) |
| Terminal | 2×1×2 | estação com mesa inclinada, 2 torres e holograma panorâmico (3 quadros) |
| Matriz de Superposição | 1×2 | rack com 4 câmaras de vidro, uma por módulo; medidores de ocupação: 10 segmentos em cada lateral, 12 na frente, luz de estado no topo (cor do tipo até 80%, âmbar até 95%, vermelho acima) |
| Colapsador | 2×2×2 | buraco negro em contenção |
| Túnel Quântico | 1×2 | transmissor com mastro e feixe curto de 1 bloco (não parece portal nem waypoint) |
| Bateria de Vácuo | 1×2 | tubo com coluna de energia; as 5 bobinas são o medidor (cada uma = 20%), anéis vermelhos abaixo de 15% e brancos cheia; arco entre terminais quando cheia |
| Processador Quântico | 1×2 | criostato com "candelabro" dourado, como os computadores quânticos reais |
| Interface Quântica | 1×1 | fica 1 bloco: precisa caber entre máquinas, canos e baús |
| Interface (painel) | painel 14×14×3 px numa face | moldura com cantos reforçados, porta redonda em degraus com janela de vidro e cristal, 4 setas; tudo em JSON |
| Âncoras | chip 8×8×2 px numa face | **o ícone 3D identifica o tipo**, sem peças a girar |

Formas das Âncoras (também no ícone do item): Máquina = hexágono ciano; Inventário = quadrado com
cantos cortados laranja; Emissor de Nível = triângulo vermelho (sugestão); Colocador/Quebrador =
losango roxo (substituído pelo Manipulador Quântico, bloco inteiro). Cada Âncora é um **chip de 8×8×2 px** colado na face (substrato, 20 pinos
dourados, tampa metálica, marca do pino 1); o tipo vai num **ícone 3D em pixel art** sobre a tampa (engrenagem = Máquina, mini baú 3D = Inventário,
barras de nível = Emissor, picareta = Colocador/Quebrador). Ao transferir, o ícone brilha suavemente e as
pistas acendem (textura animada, sem BlockEntityRenderer).

Núcleo: coroa aberta (sem cruz por cima), uma gaiola no topo de cada pilar com um orbe roxo a flutuar e uma auréola octogonal roxa a pairar
sobre a coroa; esfera central em tons de
roxo. Entrelaçador: bastão com anel octogonal, orbe roxo e um par de partículas sempre espelhadas.

**Monitor**: mesmo corpo do Monitor do TC Colony Bridge (cubo, laterais escuras com ranhuras); a frente é um
ecrã limpo só com o fluxo (seta + /s), o ícone do item e a quantidade — sem nome nem outros dados.
**Painel da Rede**: monitores multibloco como no Colony Bridge (1×1 até 8×4); o conteúdo cresce com o tamanho
(números → gráfico de operações/s → ocupação por tipo). A infraestrutura de monitores vai para o TC Core
(segundo mod a usá-la).
Modelos 3D próprios: **Regulador** (base com LEDs de estado, tubo octogonal com marcas, coluna de nível,
anel laranja do alvo e, no topo, o item regulado a flutuar e girar como um item largado no chão — desenhado
com o renderizador de itens do jogo), **Colapsador 2×2×2** (buraco negro: esfera negra, anel de fotões virado
para quem olha, disco de acreção em faixas, 4 pilones de contenção com feixes), **Túnel Quântico** (1×2, transmissor
de entrelaçamento — de propósito sem cara de portal: base de máquina com painel das duas dimensões,
mastro com bobinas, gaiola com orbe roxo e o par entrelaçado, feixe curto de 1 bloco que desvanece, com ondas a subir — nunca vai ao céu, para não parecer um waypoint), **Terminal Portátil** (scanner de mão; recetor com
orbe roxo na lateral, holograma livre por cima).

Protótipo 3D interativo: artefato "TC Quantum Modelos". Os modelos (só as peças paradas) e as texturas foram
exportados do protótipo para `mods/quantum/src/main/resources/assets/tcquantum/` e passam a ser a fonte: os
ajustes finos são feitos no Blockbench direto nesses arquivos. Mapa arquivo → bloco e o que fica para o
BlockEntityRenderer: `mods/quantum/docs/MODELOS.md`.

## 7. Arquitetura do mod

`mods/quantum/` (modid `tcquantum`), dependências: `tccore`; opcionais: Mekanism, AE2, CC:Tweaked,
JEI/EMI, Create, Thermal...

| Pacote | Responsabilidade | JUnit? |
|---|---|---|
| `grid/` (domínio da rede) | rede, entrelaçamento, orçamento por tick, agendador | sim |
| `storage/` | pools, capacidade, `ResourceType`, aritmética saturada | **sim (núcleo)** |
| `crafting/graph/` | grafo de receitas, índice invertido, camadas do Observatório | **sim** |
| `crafting/plan/` | plano, reservas, máximo, saídas com chance | **sim** |
| `crafting/exec/` | execução, processadores | parte |
| `machine/` | reconhecimento da máquina (faces, slots), estatística de saídas observadas | **sim (regras)** |
| `integration/vanilla`, `/mekanism`, `/create`, `/jei`, `/emi`, ... | adaptadores de receitas e tipos | não |
| `block/`, `menu/`, `net/`, `client/`, `registry/`, `Config` | o habitual do workspace | validação sim |

### 7.1 Técnicas de performance

- Mapas primitivos do **fastutil** (vem com o Minecraft): `Object2LongOpenHashMap`.
- Chaves de recurso **internadas**: comparação por referência.
- **Agendador por rede** com orçamento e *backoff*: Âncora sem trabalho dorme cada vez mais
  (1 → 2 → 4 ... 40 ticks) e acorda quando algo muda.
- `BlockCapabilityCache` do NeoForge nos vizinhos das Âncoras e Interfaces.
- Reconhecimento da máquina feito **uma vez** (e quando ela avisa mudança), nunca por operação.
- Sincronização por diferenças (`CountDiff` do core), com limite de frequência.
- Observatório em thread de trabalho sobre snapshot.
- Meta do teste de carga: **50 000 tipos, 2 mil milhões de itens, 200 Âncoras ativas ≤ 2 ms/tick**.

### 7.2 O que vai para o TC Core

- **Agora:** nada obrigatório (grade, barra lateral, ghost slots, redstone, `MetricRing` já estão lá).
- **Quando surgir o segundo uso:** secções recolhíveis, editor de filtro, seletor de lados,
  agendador com backoff, formatação compacta de números grandes (`UiFormat` pode crescer para isso).

## 8. Fases

| Fase | Entrega | Resultado em jogo |
|---|---|---|
| 0 | Plano, **protótipo HTML das telas e modelos**, nome final | — |
| 1 | Núcleo, Entrelaçador, pools Itens+Fluidos, Matrizes + Módulos, Terminal (Rede), segurança | guardar e retirar sem cabos |
| 2 | Interface (bloco e painel), Âncora de Inventário, **Bateria** e energia na rede | baús e canos ligados; rede com energia |
| 3 | Fabricação, Codificador, crafts de mesa, Processador, **escolha da receita pelo rendimento** (`RecipeChooser`) | autocraft de mesa |
| 4 | **Âncora de Máquina** (reconhecimento, canais itens/fluidos/energia/redstone), **grupos de máquinas iguais e divisão de carga**, adaptadores vanilla e Mekanism, aprendizado de chances | autocraft com máquinas |
| 5 | **Observatório**, Regulador, Emissor, Portátil, Painel | a vitrine do mod |
| 6 | Matriz Química, radioativos, canal de químicos nas Âncoras | químicos e resíduo nuclear |
| 7 | JEI/EMI como guia, Create/Thermal/IE (multiblocos), Colocador/Quebrador, Túnel, CC:Tweaked, Pontes | compatibilidade ampla |

## 9. Decisões

| # | Pergunta | Decisão (2026-10-20) |
|---|---|---|
| D1 | Nome do mod | **TC Quantum** (`tcquantum`) |
| D2 | Pool por tipo em vez de células | pool (recomendação; confirmar) |
| D3 | Terminal único com abas | **sim**: Rede, Fabricação, Observatório, Padrões |
| D4 | Alcance sem fio | mesma dimensão sem limite; outras com Túnel Quântico (recomendação; confirmar) |
| D5 | `long` saturado em vez de `BigInteger` | `long` (recomendação; confirmar) |
| D6 | A rede exige energia? | **sim, a rede inteira**. Sem energia: modo de espera, armazenamento só leitura, crafts pausados, nada se perde |

## 10. Protótipo das telas

Telas interativas (abas, tooltips, estados) no canvas "TC Quantum — Interfaces" do autor:
linguagem visual (cores, ícones, tooltips, números), Terminal, Observatório, Confirmação de pedido,
Âncora de Máquina, Interface Quântica, Núcleo, Matriz e Bateria.
