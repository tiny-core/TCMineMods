package org.tinycore.quantum.grid;

import org.tinycore.quantum.storage.Amounts;

/**
 * Energia de uma rede quântica (FE) e se ela está ligada. Regra pura, sem nada do Minecraft: o block entity do
 * Núcleo chama {@link #receive} quando um cabo entrega energia e {@link #tick} uma vez por tick.
 *
 * <p>Sem energia a rede entra em <b>espera</b> (plano, decisão D6): nada se perde, só para de trabalhar. Para não
 * ficar piscando entre ligada e em espera quando a entrada é menor que o consumo, a rede só volta depois de juntar
 * energia para {@code resumeTicks} ticks de consumo (histerese).
 *
 * <p>A quantidade é {@code long} (o buffer da rede passa de 2,1 mil milhões de FE quando as Baterias chegarem);
 * a conversão para o {@code int} das APIs de energia fica com quem chama ({@link Amounts#toInt}).
 */
public final class NetworkPower {

    private long stored;
    private boolean online;

    public NetworkPower(long stored) {
        this(stored, false);
    }

    /** Estado lido do disco: a rede volta como estava (sem esperar a histerese depois de reiniciar o servidor). */
    public NetworkPower(long stored, boolean online) {
        this.stored = Math.max(0, stored);
        this.online = online;
    }

    public long stored() {
        return stored;
    }

    public boolean online() {
        return online;
    }

    /**
     * Recebe até {@code amount} FE sem passar de {@code capacity}.
     *
     * @param simulate {@code true} só calcula (o que um cabo pergunta antes de entregar)
     * @return quanto foi (ou seria) aceito
     */
    public long receive(long amount, long capacity, boolean simulate) {
        if (amount <= 0 || stored >= capacity) {
            return 0;
        }
        long accepted = Math.min(amount, capacity - stored);
        if (!simulate) {
            stored += accepted;
        }
        return accepted;
    }

    /**
     * Um tick de consumo.
     *
     * @param cost        FE por tick; 0 = rede sem consumo (sempre ligada)
     * @param resumeTicks ticks de consumo que precisam estar guardados para sair da espera
     * @return {@code true} se a rede mudou entre ligada e em espera (quem chama atualiza o bloco)
     */
    public boolean tick(long cost, long resumeTicks) {
        boolean wasOnline = online;
        if (cost <= 0) {
            online = true;
            return !wasOnline;
        }
        long needed = online ? cost : Amounts.multiply(cost, Math.max(1, resumeTicks));
        if (stored >= needed) {
            stored -= cost;
            online = true;
        } else {
            online = false;
        }
        return online != wasOnline;
    }
}
