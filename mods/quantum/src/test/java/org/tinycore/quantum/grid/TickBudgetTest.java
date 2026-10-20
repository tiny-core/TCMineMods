package org.tinycore.quantum.grid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link TickBudget}: a rede para quando o tempo do tick acaba (relógio falso, controlado pelo teste). */
class TickBudgetTest {

    private long now;

    @Test
    void stopsWhenTheBudgetIsUsed() {
        TickBudget budget = new TickBudget(() -> now);
        now = 1_000;
        budget.begin(2_000);
        assertTrue(budget.hasTime());
        now = 2_999;
        assertTrue(budget.hasTime());
        now = 3_000;
        assertFalse(budget.hasTime());
        assertEquals(2_000, budget.elapsedNanos());
    }

    @Test
    void eachTickStartsAgain() {
        TickBudget budget = new TickBudget(() -> now);
        budget.begin(100);
        now = 500;
        assertFalse(budget.hasTime());
        budget.begin(100);
        assertTrue(budget.hasTime());
    }

    @Test
    void zeroBudgetMeansNoWork() {
        TickBudget budget = new TickBudget(() -> now);
        budget.begin(0);
        assertFalse(budget.hasTime());
    }
}
