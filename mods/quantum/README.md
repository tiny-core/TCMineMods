# TC Quantum (`tcquantum`)

Armazenamento e automação **sem cabos** para NeoForge 1.21.1 (ATM10). Um **Núcleo Quântico** guarda tudo; os
outros blocos ficam entrelaçados com ele em qualquer ponto da base. Números gigantes (até 9,2 quintilhões por
recurso), crafts de mesa e de máquinas, e o **Observatório**, que mostra o que a rede consegue fabricar agora.

> Em desenvolvimento — fase 1. Plano, telas e modelos: `docs/planos/tc-quantum.md` (raiz do workspace).

## Modelos

Os modelos 3D e as texturas estão em `src/main/resources/assets/tcquantum/` e abrem no Blockbench
("Java Block/Item"). O que cada arquivo é e o que fica animado no jogo: `docs/MODELOS.md`.

## Instalar

Copie para a pasta `mods/` o `tcquantum-<versão>.jar` **e** o `tccore-<versão>.jar` (ambos em `build/libs/` da
raiz depois de `./gradlew build`).

## Config

`config/tcquantum-common.toml`:

| Chave | Padrão | O quê |
|---|---|---|
| `tickBudgetMicros` | 2000 | Tempo máximo que cada rede usa por tick (µs). O resto passa para o tick seguinte. |
| `idleBackoffMaxTicks` | 40 | Espera máxima de um bloco sem trabalho antes de olhar de novo. |
