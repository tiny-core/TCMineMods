package org.tinycore.quantum.grid;

/**
 * Espera com recuo de um bloco que trabalha sozinho (Âncora, Interface): se um ciclo não encontrou nada para fazer,
 * a próxima olhadela fica para mais tarde (1, 2, 4, 8... ticks, até um máximo); assim que aparece trabalho, volta a
 * olhar a cada tick. Blocos ociosos quase não gastam tempo do servidor. É a mesma ideia do
 * {@code TickRateModulation} do AE2.
 * <p>
 * Regra pura, sem mundo nem tempo real: quem usa chama {@link #tick()} uma vez por tick do jogo.
 */
public final class Backoff {

    private final int maxTicks;
    private int interval = 1;
    private int countdown;

    /** @param maxTicks espera máxima entre duas olhadelas (ver {@code idleBackoffMaxTicks} na config) */
    public Backoff(int maxTicks) {
        this.maxTicks = Math.max(1, maxTicks);
    }

    /** Chamado a cada tick: {@code true} quando é hora de trabalhar. */
    public boolean tick() {
        if (countdown > 0) {
            countdown--;
            return false;
        }
        return true;
    }

    /** Depois de um ciclo: se trabalhou, volta a 1 tick; se não, dobra a espera (até o máximo). */
    public void done(boolean didWork) {
        interval = didWork ? 1 : Math.min(maxTicks, interval * 2);
        countdown = interval - 1;
    }

    /** Acorda já (ex.: o inventário ao lado mudou): a próxima chamada de {@link #tick()} trabalha. */
    public void wake() {
        interval = 1;
        countdown = 0;
    }

    /** Espera atual entre olhadelas, em ticks. */
    public int interval() {
        return interval;
    }
}
