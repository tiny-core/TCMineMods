# TC Core — instruções específicas

As regras gerais (autor, idioma, arquitetura, segurança, desempenho, documentação, git) estão no `CLAUDE.md` da
raiz. Aqui, só o que é do core.

Biblioteca comum dos mods TC, instalada em jogo como **mod separado** (`tccore`). O que cada pacote oferece está no
`README.md`; as decisões do core ficam no `docs/DECISOES.md` **da raiz** (são do workspace).

## Regras que não podem ser quebradas

- O core **nunca** importa um mod TC nem um mod externo (AE2, MineColonies, Mekanism).
- Mudança de API pública (classe, método, tradução) pode quebrar todos os mods: rode `./gradlew build` na raiz e
  suba `mod_version` em `gradle.properties`; se um mod passar a exigir a versão nova, ajuste o
  `tccore_version_range` dele e anote no `CHANGELOG.md`.
- Textos do core: chaves em `assets/tccore/lang` (en_us, pt_br, pt_pt).

## Comandos (na raiz do workspace)

```bash
./gradlew :core:build
./gradlew :core:test --rerun
```
