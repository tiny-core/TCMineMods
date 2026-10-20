# libs/ — jars do ATM10 para compilar e testar

Copie para esta pasta, a partir da pasta `mods/` do seu ATM10 (as mesmas versões do modpack):

| Jar | Usado por |
|---|---|
| `minecolonies-*.jar`, `structurize-*.jar`, `blockui-*.jar`, `domum_ornamentum-*.jar` | Colony Bridge (compilar e runClient) |
| `appliedenergistics2-*.jar`, `guideme-*.jar` (dependência do AE2) | Colony Bridge e Cloud Storage (runClient) |
| `jei-*.jar` | Colony Bridge (opcional; arrastar itens para o filtro) |

Os jars **não** vão para o git (`.gitignore`). Outra pasta: `./gradlew ... -Plibs_dir=/caminho`.
Sem os jars do MineColonies o Colony Bridge não compila; os outros mods compilam normalmente.
