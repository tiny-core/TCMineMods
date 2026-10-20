package org.tinycore.quantum.grid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link NetworkPower}: receber até a capacidade, consumir por tick e a espera sem piscar. */
class NetworkPowerTest {

    @Test
    void receiveStopsAtCapacityAndSimulateChangesNothing() {
        NetworkPower power = new NetworkPower(0);
        assertEquals(100, power.receive(100, 150, true));
        assertEquals(0, power.stored(), "simulação não guarda");
        assertEquals(100, power.receive(100, 150, false));
        assertEquals(50, power.receive(100, 150, false), "só o que cabe");
        assertEquals(0, power.receive(100, 150, false), "cheia");
        assertEquals(150, power.stored());
    }

    @Test
    void storedAboveCapacityAcceptsNothing() {
        NetworkPower power = new NetworkPower(500); // capacidade diminuída na config depois de cheia
        assertEquals(0, power.receive(10, 100, false));
        assertEquals(500, power.stored(), "não perde o que já tinha");
    }

    @Test
    void negativeOrZeroAmountsAreIgnored() {
        NetworkPower power = new NetworkPower(-5);
        assertEquals(0, power.stored());
        assertEquals(0, power.receive(-10, 100, false));
        assertEquals(0, power.receive(0, 100, false));
    }

    @Test
    void consumesEveryTickWhileOnline() {
        NetworkPower power = new NetworkPower(1_000);
        assertTrue(power.tick(10, 20), "liga: 1000 ≥ 10 × 20");
        assertTrue(power.online());
        assertFalse(power.tick(10, 20), "continua ligada, sem mudança");
        assertEquals(980, power.stored());
    }

    @Test
    void goesToStandbyWithoutLosingEnergy() {
        NetworkPower power = new NetworkPower(15);
        power.tick(10, 1);
        assertTrue(power.online());
        assertTrue(power.tick(10, 1), "5 < 10: entra em espera");
        assertFalse(power.online());
        assertEquals(5, power.stored(), "a espera não consome nada");
    }

    @Test
    void resumesOnlyAfterStoringSeveralTicksOfEnergy() {
        NetworkPower power = new NetworkPower(0);
        power.tick(10, 5);
        assertFalse(power.online());
        power.receive(40, 1_000, false);
        assertFalse(power.tick(10, 5), "40 < 50: ainda em espera (não pisca)");
        power.receive(10, 1_000, false);
        assertTrue(power.tick(10, 5), "50 ≥ 50: volta");
        assertEquals(40, power.stored());
    }

    @Test
    void zeroCostKeepsTheNetworkOnline() {
        NetworkPower power = new NetworkPower(0);
        assertTrue(power.tick(0, 100));
        assertTrue(power.online());
        assertEquals(0, power.stored());
    }

    @Test
    void hugeResumeThresholdDoesNotOverflow() {
        NetworkPower power = new NetworkPower(Long.MAX_VALUE - 1);
        assertFalse(power.tick(Long.MAX_VALUE, Long.MAX_VALUE));
        assertFalse(power.online());
    }
}
