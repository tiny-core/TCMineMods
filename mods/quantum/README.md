# TC Quantum (`tcquantum`)

Armazenamento e automação **sem cabos** para NeoForge 1.21.1 (ATM10). Um **Núcleo Quântico** guarda tudo; os
outros blocos ficam entrelaçados com ele em qualquer ponto da base. Números gigantes (até 9,2 quintilhões por
recurso), crafts de mesa e de máquinas, e o **Observatório**, que mostra o que a rede consegue fabricar agora.

> Em desenvolvimento — fase 1 (fatia 1.2: o Núcleo Quântico). Plano, telas e modelos: `docs/PLANO.md`.

## TC Núcleo Quântico

O coração da rede: sem ele, nada do mod funciona. Ocupa um cubo de **3×3×3** blocos.

- **Colocar:** o bloco clicado vira a base do cubo (clicando num teto, ele cresce para baixo). Precisa das 27 posições
  livres e de ninguém dentro; se não couber, aparece um aviso.
- **Energia:** ligue um cabo de energia (FE) em qualquer face do cubo. A rede consome `coreEnergyPerTick` FE por tick
  e o Núcleo guarda até `coreEnergyCapacity` FE. Sem energia, a rede fica **em espera**: não perde nada, só para, e
  volta sozinha depois de juntar energia para alguns segundos (`standbyResumeTicks`).
- **Ver o estado:** clique com a mão vazia: o chat mostra se está ligado, a energia e o id da rede (até o Terminal
  ficar pronto).
- **Mudar de lugar:** quebrar qualquer parte quebra o cubo e devolve **um** item do Núcleo. O item guarda a rede
  (aparece no tooltip): colocado de novo, em qualquer lugar ou dimensão, a energia e o conteúdo voltam. Um Núcleo
  copiado no criativo cria uma rede nova; duas cópias nunca dividem o mesmo conteúdo.
- Resiste a explosões como obsidiana e não é movido por pistões.

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
| `coreEnergyCapacity` | 1000000 | Energia que o Núcleo guarda (FE). |
| `coreEnergyPerTick` | 16 | Consumo base da rede (FE/t). 0 = sem consumo. |
| `standbyResumeTicks` | 100 | Em espera, a rede só volta com energia para este número de ticks de consumo. |
