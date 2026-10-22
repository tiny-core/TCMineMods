---
description: Propõe a(s) mensagem(ns) de commit para as mudanças atuais, sem commitar
allowed-tools: Bash(git status:*), Bash(git diff:*)
---
Leia `git status --short` e `git diff --stat` (e o diff só onde precisar entender a mudança). NÃO rode `git add`, `git commit` nem `git push`.

Responda em PT-BR com:
1. **O que mudou** — até 6 linhas, por arquivo ou grupo de arquivos.
2. **Commits sugeridos** — se as mudanças forem de temas diferentes, divida em commits pequenos e diga quais arquivos vão em cada um. Para cada commit, a mensagem em inglês no formato do repo (`tipo(escopo): descrição` no imperativo, até 72 caracteres; corpo opcional explicando o porquê) dentro de um bloco de código, pronta para copiar, seguida dos comandos `git add <arquivos>` correspondentes.
3. **O que testar em jogo** antes de commitar, se houver lógica de jogo (passos curtos e o resultado esperado).
