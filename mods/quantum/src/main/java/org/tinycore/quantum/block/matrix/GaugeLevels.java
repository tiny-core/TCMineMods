package org.tinycore.quantum.block.matrix;

/**
 * Regras dos medidores de ocupação da Matriz (plano, §6.1): quanto está cheio, quantos segmentos acendem e em que
 * faixa de cor. Pura: o servidor usa {@link #percent} para decidir quando mandar o dado ao cliente, e o renderizador
 * usa o resto para desenhar.
 *
 * <ul>
 *   <li>Segmentos acesos: proporcionais à ocupação; qualquer coisa guardada acende pelo menos um.</li>
 *   <li>Faixas: cor do tipo até 80%, âmbar até 95%, vermelho acima ({@link Band}).</li>
 *   <li>Cheia (100% ou acima da capacidade): o último segmento pisca.</li>
 * </ul>
 */
public final class GaugeLevels {

    /** Faixa de cor do medidor e da luz de estado. */
    public enum Band { NORMAL, HIGH, FULL }

    private GaugeLevels() {}

    /**
     * Ocupação em porcentagem inteira (0 a 100). Acima da capacidade conta como 100. Sem capacidade: 0 se vazia,
     * 100 se tem algo (Matriz sem capacidade nenhuma com itens guardados está "cheia").
     */
    public static int percent(long stored, long capacity) {
        if (stored <= 0) {
            return 0;
        }
        if (capacity <= 0 || stored >= capacity) {
            return 100;
        }
        int percent = (int) Math.floor((double) stored * 100.0 / capacity);
        return Math.max(1, Math.min(99, percent));
    }

    /** Quantos de {@code segments} segmentos acendem com {@code percent}% de ocupação. */
    public static int litSegments(int percent, int segments) {
        if (percent <= 0) {
            return 0;
        }
        int lit = percent * segments / 100;
        return Math.max(1, Math.min(segments, lit));
    }

    public static Band band(int percent) {
        if (percent >= 95) {
            return Band.FULL;
        }
        return percent >= 80 ? Band.HIGH : Band.NORMAL;
    }

    /** Cheia: o último segmento pisca. */
    public static boolean blinksLast(int percent) {
        return percent >= 100;
    }
}
