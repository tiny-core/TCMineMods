package org.tinycore.quantum.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link Amounts}: os números gigantes param no máximo em vez de dar a volta para negativo — senão uma rede cheia
 * mostraria quantidades negativas e aceitaria itens infinitos.
 */
class AmountsTest {

    @Test
    void addSaturatesAtMax() {
        assertEquals(Amounts.MAX, Amounts.add(Amounts.MAX, 1));
        assertEquals(Amounts.MAX, Amounts.add(Amounts.MAX - 5, 10));
        assertEquals(15, Amounts.add(5, 10));
    }

    @Test
    void subtractStopsAtZero() {
        assertEquals(0, Amounts.subtract(5, 10));
        assertEquals(5, Amounts.subtract(15, 10));
    }

    @Test
    void negativeInputsCountAsZero() {
        assertEquals(10, Amounts.add(-3, 10));
        assertEquals(7, Amounts.subtract(7, -2));
    }

    @Test
    void multiplySaturates() {
        assertEquals(64L * 1_000_000_000L, Amounts.multiply(64, 1_000_000_000L));
        assertEquals(Amounts.MAX, Amounts.multiply(Long.MAX_VALUE / 2, 3));
        assertEquals(0, Amounts.multiply(0, 99));
    }

    @Test
    void toIntNeverPassesTheIntLimit() {
        assertEquals(64, Amounts.toInt(64));
        assertEquals(Integer.MAX_VALUE, Amounts.toInt(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, Amounts.toInt(Integer.MAX_VALUE + 1L), "o Minecraft não aceita mais");
        assertEquals(Integer.MAX_VALUE, Amounts.toInt(Amounts.MAX));
        assertEquals(0, Amounts.toInt(-5));
    }

    @Test
    void fractionIsClampedBetweenZeroAndOne() {
        assertEquals(0.5, Amounts.fraction(50, 100), 1e-9);
        assertEquals(1.0, Amounts.fraction(150, 100), 1e-9, "acima da capacidade conta como cheio");
        assertEquals(0.0, Amounts.fraction(10, 0), 1e-9);
    }
}
