package org.tinycore.quantum.block.matrix;

import org.tinycore.quantum.storage.Amounts;

import java.util.Arrays;

/**
 * Os encaixes de Módulos de Qubits de uma Matriz (um por câmara de vidro do modelo). Regra pura: cada encaixe guarda
 * o nível do módulo (1 a {@link #MAX_TIER}) ou 0 se está vazio; a capacidade da Matriz é a base mais a soma dos
 * módulos, saturada em {@link Amounts#MAX}.
 *
 * <p>Os módulos entram no primeiro encaixe vazio e saem do último ocupado (como uma pilha), o que basta sem tela.
 */
public final class MatrixModules {

    /** Encaixes por Matriz (as 4 câmaras do modelo). */
    public static final int SLOTS = 4;
    /** Maior nível de módulo (Módulo de Qubits V). */
    public static final int MAX_TIER = 5;

    private final int[] tiers = new int[SLOTS];

    /**
     * Encaixa um módulo no primeiro encaixe vazio.
     *
     * @return {@code false} se o nível é inválido ou a Matriz está cheia
     */
    public boolean insert(int tier) {
        if (tier < 1 || tier > MAX_TIER) {
            return false;
        }
        for (int i = 0; i < SLOTS; i++) {
            if (tiers[i] == 0) {
                tiers[i] = tier;
                return true;
            }
        }
        return false;
    }

    /**
     * Tira o último módulo encaixado.
     *
     * @return o nível dele, ou 0 se não havia nenhum
     */
    public int removeLast() {
        for (int i = SLOTS - 1; i >= 0; i--) {
            if (tiers[i] != 0) {
                int tier = tiers[i];
                tiers[i] = 0;
                return tier;
            }
        }
        return 0;
    }

    public int count() {
        int count = 0;
        for (int tier : tiers) {
            if (tier != 0) {
                count++;
            }
        }
        return count;
    }

    public boolean isFull() {
        return count() == SLOTS;
    }

    /**
     * Capacidade da Matriz.
     *
     * @param base         capacidade sem módulos
     * @param perTier      capacidade que cada nível soma ({@code perTier[0]} = nível I)
     */
    public long capacity(long base, long[] perTier) {
        long total = Math.max(0, base);
        for (int tier : tiers) {
            if (tier != 0 && tier <= perTier.length) {
                total = Amounts.add(total, perTier[tier - 1]);
            }
        }
        return total;
    }

    /** Cópia dos níveis por encaixe (para gravar no disco). */
    public int[] toArray() {
        return tiers.clone();
    }

    /** Lê do disco, ignorando encaixes a mais e níveis inválidos (dado vindo de outra versão do mod). */
    public void load(int[] saved) {
        Arrays.fill(tiers, 0);
        for (int i = 0; i < Math.min(SLOTS, saved.length); i++) {
            int tier = saved[i];
            tiers[i] = tier >= 1 && tier <= MAX_TIER ? tier : 0;
        }
    }

    /** Níveis encaixados, em ordem (para mensagens: "I, II, V"). */
    public int[] installed() {
        return Arrays.stream(tiers).filter(t -> t != 0).toArray();
    }
}
