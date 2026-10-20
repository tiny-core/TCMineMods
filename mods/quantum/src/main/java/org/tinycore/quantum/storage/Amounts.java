package org.tinycore.quantum.storage;

/**
 * Aritmética das quantidades da rede: {@code long} com <b>saturação</b>. Em vez de "dar a volta" para negativo quando
 * passa do máximo (o que um {@code long} comum faz em silêncio), a conta para em {@link #MAX}. É o que permite
 * números gigantes (até 9,2 quintilhões por recurso) com a velocidade de uma soma comum da CPU.
 * <p>
 * Por que não {@code BigInteger}: cada soma criaria um objeto novo, 10 a 50 vezes mais lenta e com lixo para o
 * coletor de memória — exatamente o que causa lag numa rede grande (decisão em {@code docs/DECISOES.md} do mod).
 * <p>
 * Quantidades nunca são negativas: a rede não "deve" itens a ninguém. Entradas negativas contam como zero.
 * <p>
 * {@link #MAX} é o maior inteiro que o Java guarda num {@code long}: não há como passar dele. Fora do pool, o
 * Minecraft e os outros mods usam {@code int} (pilhas, fluidos, energia): ver {@link #toInt}.
 */
public final class Amounts {

    /** Maior quantidade que a rede guarda de um recurso (e maior capacidade). */
    public static final long MAX = Long.MAX_VALUE;

    private Amounts() {}

    /** {@code a + b} parando em {@link #MAX}; negativos contam como zero. */
    public static long add(long a, long b) {
        a = Math.max(0, a);
        b = Math.max(0, b);
        long sum = a + b;
        // Soma de dois positivos que fica negativa = passou do máximo ("overflow").
        return sum < 0 ? MAX : sum;
    }

    /** {@code a - b} parando em zero; negativos contam como zero. */
    public static long subtract(long a, long b) {
        a = Math.max(0, a);
        b = Math.max(0, b);
        return b >= a ? 0 : a - b;
    }

    /** {@code a * b} parando em {@link #MAX} (ex.: capacidade de um módulo × quantidade de módulos). */
    public static long multiply(long a, long b) {
        if (a <= 0 || b <= 0) {
            return 0;
        }
        return a > MAX / b ? MAX : a * b;
    }

    /**
     * Converte para {@code int}, cortando no máximo do {@code int} (2 147 483 647). Usar em <b>toda</b> passagem do
     * pool para uma API do Minecraft ou de outro mod que só aceita {@code int} (pilhas de itens, fluidos em mB, energia
     * FE): o pool vai até 9,2 quintilhões, essas APIs não. Negativos viram zero.
     */
    public static int toInt(long amount) {
        if (amount <= 0) {
            return 0;
        }
        return amount >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    /** Fração {@code part / total} entre 0 e 1 (para medidores); 0 quando {@code total} é zero. */
    public static double fraction(long part, long total) {
        if (total <= 0 || part <= 0) {
            return 0;
        }
        return part >= total ? 1 : (double) part / (double) total;
    }
}
