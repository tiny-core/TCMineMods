package org.tinycore.quantum.grid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link Backoff}: bloco ocioso olha cada vez menos; com trabalho (ou acordado), volta a olhar a cada tick. */
class BackoffTest {

    /** Ticks até a próxima olhadela (incluindo a própria). */
    private static int ticksUntilWork(Backoff backoff) {
        int ticks = 1;
        while (!backoff.tick()) {
            ticks++;
        }
        return ticks;
    }

    @Test
    void idleDoublesUpToTheMaximum() {
        Backoff backoff = new Backoff(8);
        assertTrue(backoff.tick());
        backoff.done(false);
        assertEquals(2, ticksUntilWork(backoff));
        backoff.done(false);
        assertEquals(4, ticksUntilWork(backoff));
        backoff.done(false);
        assertEquals(8, ticksUntilWork(backoff));
        backoff.done(false);
        assertEquals(8, ticksUntilWork(backoff), "não passa do máximo");
    }

    @Test
    void workResetsToEveryTick() {
        Backoff backoff = new Backoff(40);
        backoff.done(false);
        backoff.done(false);
        backoff.done(true);
        assertEquals(1, backoff.interval());
        assertEquals(1, ticksUntilWork(backoff));
    }

    @Test
    void wakeWorksOnTheNextTick() {
        Backoff backoff = new Backoff(40);
        for (int i = 0; i < 5; i++) {
            backoff.done(false);
        }
        backoff.wake();
        assertTrue(backoff.tick());
    }
}
