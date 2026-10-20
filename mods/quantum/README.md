# TC Quantum (`tcquantum`)

Armazenamento e automação **sem cabos** para NeoForge 1.21.1 (ATM10). Um **Núcleo Quântico** guarda tudo; os
outros blocos ficam entrelaçados com ele em qualquer ponto da base. Números gigantes (até 9,2 quintilhões por
recurso), crafts de mesa e de máquinas, e o **Observatório**, que mostra o que a rede consegue fabricar agora.

> Em desenvolvimento — fase 1 (fatia 1.3: Entrelaçador e Matriz). Plano, telas e modelos: `docs/PLANO.md`.

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

## TC Entrelaçador

Liga blocos à rede de um Núcleo, sem cabos.

1. **Sintonizar:** clique em qualquer parte de um Núcleo. O item passa a brilhar e o tooltip mostra a rede.
2. **Ligar um bloco:** clique no bloco (por enquanto, a Matriz). Mensagem: "… entrou na rede …".
3. **Tirar da rede:** Shift + clique no bloco. **Esquecer a rede:** Shift + usar no ar.
4. **Ver as ligações:** com o Entrelaçador sintonizado na mão (ou na outra mão), pontos coloridos correm de cada
   bloco até o Núcleo (até `entangleLinesRange` blocos de você).

Blocos colocados até `autoEntangleRadius` blocos de um Núcleo entram sozinhos na rede dele.

## TC Matriz de Superposição (Itens)

Dois blocos de altura; soma `matrixItemCapacity` itens à capacidade da rede em que estiver. Não guarda itens ela
mesma: o conteúdo é da rede, e a capacidade continua a contar com o chunk da Matriz descarregado. Clique no Núcleo
para ver quantas Matrizes a rede tem e a capacidade total. Guardar e tirar itens chega com o Terminal (fatia 1.5);
os Módulos de Qubits, que aumentam a capacidade, na 1.4.

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
| `matrixItemCapacity` | 65536 | Itens que cada Matriz soma à rede (sem módulos). |
| `autoEntangleRadius` | 16 | Bloco colocado até esta distância de um Núcleo entra sozinho na rede. 0 = só pelo Entrelaçador. |
| `entangleLinesRange` | 48 | Distância (blocos) das ligações mostradas com o Entrelaçador na mão. |
