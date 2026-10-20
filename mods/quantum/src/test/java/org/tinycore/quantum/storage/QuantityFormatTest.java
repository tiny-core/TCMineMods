package org.tinycore.quantum.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** {@link QuantityFormat}: texto curto para números grandes, sem nunca arredondar para cima. */
class QuantityFormatTest {

    @Test
    void smallNumbersStayAsTheyAre() {
        assertEquals("0", QuantityFormat.compact(0));
        assertEquals("999", QuantityFormat.compact(999));
    }

    @Test
    void thousandsAndMillions() {
        assertEquals("1k", QuantityFormat.compact(1_000));
        assertEquals("1.2k", QuantityFormat.compact(1_234));
        assertEquals("12.3k", QuantityFormat.compact(12_345));
        assertEquals("123k", QuantityFormat.compact(123_456));
        assertEquals("1M", QuantityFormat.compact(1_000_000));
        assertEquals("3.4M", QuantityFormat.compact(3_400_000));
    }

    @Test
    void neverRoundsUp() {
        assertEquals("999k", QuantityFormat.compact(999_999));
        assertEquals("1.9k", QuantityFormat.compact(1_999));
    }

    @Test
    void reachesTheLargestLong() {
        assertEquals("1T", QuantityFormat.compact(1_000_000_000_000L));
        assertEquals("9.2E", QuantityFormat.compact(Long.MAX_VALUE));
    }

    @Test
    void negativeValuesKeepTheSign() {
        assertEquals("-1.2k", QuantityFormat.compact(-1_234));
        assertEquals("-9.2E", QuantityFormat.compact(Long.MIN_VALUE));
    }
}
