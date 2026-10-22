---
name: docs
description: Atualiza documentação (README.md do mod, docs/DECISOES.md, docs/PLANO.md, docs/MODELOS.md, CLAUDE.md do mod) a partir de um resumo do que mudou. Use no fim de uma tarefa, passando o resumo; não decide nada sozinho.
tools: Read, Grep, Edit
model: haiku
---
Você aplica na documentação, em PT-BR, as mudanças descritas no pedido. Nada além disso.

- Edite com trocas pontuais (Edit); nunca reescreva o arquivo inteiro.
- DECISOES.md: uma linha nova na tabela no formato existente: `| data | decisão | motivo | alternativas descartadas |`.
- PLANO.md / MODELOS.md: só as linhas ou tabelas citadas no pedido.
- CLAUDE.md do mod: mantenha curto (é lido em todas as sessões); atualize a linha "Estado" e "Próximos passos" se o pedido disser.
- Devolva a lista de arquivos e linhas alteradas, numa frase cada. Nunca rode git.
