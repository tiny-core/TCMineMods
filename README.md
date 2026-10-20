# TCMineMods

Mods **TC** (tiny-core) para **NeoForge 1.21.1** (Java 21), feitos para o modpack **ATM10**. Fazem parte do
ecossistema [TCMine](https://github.com/tiny-core/TCMine) (painel, launcher e nuvem de itens).

| Mod | Pasta | O que faz | Estado |
|---|---|---|---|
| **TC Core** (`tccore`) | `mods/core/` | Biblioteca comum: design system das telas, ghost slots, redstone, estatísticas. Obrigatório em jogo para os outros mods TC. | estável |
| **TC Colony Bridge** (`tccolonybridge`) | `mods/colony-bridge/` | Liga uma rede ME do AE2 aos pedidos do MineColonies (Ponte, Abastecedor, Monitores, Terminal do Armazém). | em uso |
| **TC Cloud Storage** (`tccloud`) | `mods/cloud-storage/` | Itens guardados na nuvem do TCMine e vistos pela rede AE2; reencontrados em outro servidor do mesmo dono. | em desenvolvimento |
| **TC Quantum** (`tcquantum`) | `mods/quantum/` | Armazenamento e automação sem cabos: Núcleo Quântico, blocos entrelaçados, números gigantes e o Observatório de crafts. | em desenvolvimento |

Planejado: mod para gerenciar os reatores do Mekanism, no mesmo estilo.

## Instalar no jogo

Copie para a pasta `mods/` do ATM10 o jar do mod **e** o `tccore-<versão>.jar` (todos em `build/libs/` depois do
build). Cada mod explica o uso no próprio `README.md`.

## Estrutura

```
TCMineMods/
├── build-logic/          # plugin "tc.mod": o que todo mod repete (NeoForge, TC Core, runs, testes, mods.toml)
├── mods/
│   └── <mod>/
│       ├── build.gradle        # plugins { id 'tc.mod' } + dependências próprias
│       ├── gradle.properties   # mod_id, nome, versão, pacote
│       ├── README.md           # uso (jogador)
│       ├── CLAUDE.md           # regras do mod para o Claude Code
│       ├── docs/               # PLANO.md, DECISOES.md (e outros do mod)
│       └── src/                # main/java, main/resources, main/templates (mods.toml), test/java
├── libs/                 # jars do ATM10 para compilar e testar (fora do git; ver libs/LEIA-ME.md)
├── docs/DECISOES.md      # decisões do workspace (build, core)
├── tools/                # scripts auxiliares (ícones das telas)
├── gradle.properties     # versões comuns (Minecraft, NeoForge, AE2) e licença
└── CHANGELOG.md
```

## Build

O Gradle roda sempre com o **JDK 21** (`gradle/gradle-daemon-jvm.properties`), mesmo que a máquina tenha um Java mais
novo: precisa de um JDK 21 instalado (ou já baixado pelo Gradle em `~/.gradle/jdks`). No IntelliJ: *Settings → Build →
Build Tools → Gradle → Gradle JVM* = 21.

```bash
./gradlew build                      # compila e testa tudo; jars em build/libs/
./gradlew test --rerun               # só os testes (resultado de cada um no terminal)
./gradlew :quantum:runClient         # Minecraft de desenvolvimento com o mod (o core roda junto)
./gradlew :quantum:runServer         # servidor de desenvolvimento (-Pserver_port=25566 para uma segunda porta)
```

O Colony Bridge só compila com os jars do MineColonies em `libs/` (ver `libs/LEIA-ME.md`).

## IntelliJ IDEA

1. **File → Open** na pasta raiz e confirme o projeto Gradle. A primeira importação baixa o Minecraft e demora.
2. Execuções: depois da sincronização do Gradle, o ModDevGradle cria as configurações de execução de cada mod
   (seletor no topo da janela). Também dá pela janela **Gradle** → `<mod>` → `runClient`/`runServer`.
3. **Formatar ao salvar** (uma vez por máquina): *Settings → Tools → Actions on Save* → marque **Reformat code**
   (só linhas alteradas: *Changed lines*) e **Optimize imports**. O estilo vem do `.editorconfig` do repositório,
   então todos formatam igual.

## Criar um mod novo

1. Pasta `mods/<nome>/` (ex.: `mods/reactor/`) com:
   - `build.gradle`: `plugins { id 'tc.mod' }` e as dependências próprias (o TC Core já vem pelo plugin);
   - `gradle.properties`: `mod_id`, `mod_name` ("TC ..."), `mod_version`, `mod_group_id`, `tccore_version_range`;
   - `src/main/templates/META-INF/neoforge.mods.toml` (copie de `mods/quantum/`);
   - `README.md`, `CLAUDE.md` e `docs/DECISOES.md`.
2. O nome na lista de mods do `settings.gradle` (o projeto fica `:reactor`).
3. Uma linha na tabela acima.

## Licença

[GPL-3.0](LICENSE), a mesma do TCMine.
