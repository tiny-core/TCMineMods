package org.tinycore.quantum.block.matrix;

import org.junit.jupiter.api.Test;
import org.tinycore.quantum.block.matrix.GaugeLevels.Band;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link GaugeLevels}: porcentagem, segmentos acesos, faixa de cor e o pisca de cheia. */
class GaugeLevelsTest {

    @Test
    void percentNeverShowsEmptyWithSomethingStoredNorFullBeforeItIs() {
        assertEquals(0, GaugeLevels.percent(0, 1_000));
        assertEquals(1, GaugeLevels.percent(1, 1_000_000), "um item acende pelo menos 1%");
        assertEquals(99, GaugeLevels.percent(999_999, 1_000_000), "quase cheia não é cheia");
        assertEquals(100, GaugeLevels.percent(1_000, 1_000));
        assertEquals(100, GaugeLevels.percent(2_000, 1_000), "acima da capacidade conta como cheia");
    }

    @Test
    void percentWithoutCapacity() {
        assertEquals(0, GaugeLevels.percent(0, 0));
        assertEquals(100, GaugeLevels.percent(5, 0));
    }

    @Test
    void percentWithHugeNumbersDoesNotOverflow() {
        assertEquals(50, GaugeLevels.percent(Long.MAX_VALUE / 2, Long.MAX_VALUE - 1));
    }

    @Test
    void litSegments() {
        assertEquals(0, GaugeLevels.litSegments(0, 10));
        assertEquals(1, GaugeLevels.litSegments(1, 10), "qualquer coisa guardada acende um");
        assertEquals(5, GaugeLevels.litSegments(50, 10));
        assertEquals(6, GaugeLevels.litSegments(50, 12));
        assertEquals(10, GaugeLevels.litSegments(100, 10));
    }

    @Test
    void bands() {
        assertEquals(Band.NORMAL, GaugeLevels.band(79));
        assertEquals(Band.HIGH, GaugeLevels.band(80));
        assertEquals(Band.HIGH, GaugeLevels.band(94));
        assertEquals(Band.FULL, GaugeLevels.band(95));
    }

    @Test
    void onlyAFullMatrixBlinks() {
        assertFalse(GaugeLevels.blinksLast(99));
        assertTrue(GaugeLevels.blinksLast(100));
    }
}
