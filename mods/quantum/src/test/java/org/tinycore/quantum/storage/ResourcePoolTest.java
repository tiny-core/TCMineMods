package org.tinycore.quantum.storage;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ResourcePool}: o coração do armazenamento. Tem de respeitar a capacidade, nunca criar nem perder recursos,
 * simular sem mexer e marcar só o que mudou para a sincronização com o cliente.
 */
class ResourcePoolTest {

    private static ResourcePool<String> pool(long capacity) {
        ResourcePool<String> pool = new ResourcePool<>();
        pool.setCapacity(capacity);
        return pool;
    }

    @Test
    void insertIsLimitedByFreeSpace() {
        ResourcePool<String> pool = pool(100);
        assertEquals(60, pool.insert("ferro", 60, false));
        assertEquals(40, pool.insert("ouro", 70, false), "só cabem 40");
        assertEquals(100, pool.stored());
        assertEquals(0, pool.free());
        assertEquals(2, pool.types());
    }

    @Test
    void simulateNeverChangesAnything() {
        ResourcePool<String> pool = pool(100);
        assertEquals(50, pool.insert("ferro", 50, true));
        assertEquals(0, pool.stored());
        assertFalse(pool.hasChanges());
        pool.insert("ferro", 30, false);
        pool.drainChanges((k, v) -> { });
        assertEquals(30, pool.extract("ferro", 99, true));
        assertEquals(30, pool.amount("ferro"));
        assertFalse(pool.hasChanges());
    }

    @Test
    void extractRemovesTheKeyWhenItRunsOut() {
        ResourcePool<String> pool = pool(1000);
        pool.insert("ferro", 64, false);
        assertEquals(10, pool.extract("ferro", 10, false));
        assertEquals(54, pool.amount("ferro"));
        assertEquals(54, pool.extract("ferro", 100, false), "só havia 54");
        assertEquals(0, pool.types());
        assertEquals(0, pool.stored());
    }

    @Test
    void nothingIsCreatedOrLost() {
        ResourcePool<String> pool = pool(500);
        long in = pool.insert("a", 300, false) + pool.insert("b", 300, false);
        long out = pool.extract("a", 120, false) + pool.extract("b", 50, false) + pool.extract("c", 10, false);
        assertEquals(in - out, pool.stored());
        assertEquals(pool.amount("a") + pool.amount("b"), pool.stored());
    }

    @Test
    void overCapacityOnlyAllowsExtraction() {
        ResourcePool<String> pool = pool(100);
        pool.insert("ferro", 100, false);
        pool.setCapacity(40); // uma Matriz foi partida
        assertTrue(pool.overCapacity());
        assertEquals(0, pool.insert("ferro", 1, false));
        assertEquals(100, pool.amount("ferro"), "nada é destruído");
        assertEquals(70, pool.extract("ferro", 70, false));
        assertFalse(pool.overCapacity());
        assertEquals(10, pool.insert("ferro", 50, false));
    }

    @Test
    void hugeAmountsSaturateInsteadOfOverflowing() {
        ResourcePool<String> pool = pool(Amounts.MAX);
        assertEquals(Amounts.MAX - 10, pool.insert("pedregulho", Amounts.MAX - 10, false));
        assertEquals(10, pool.insert("terra", Long.MAX_VALUE, false), "só restavam 10");
        assertEquals(Amounts.MAX, pool.stored());
        assertEquals(0, pool.insert("pedregulho", 1, false));
    }

    @Test
    void invalidAmountsDoNothing() {
        ResourcePool<String> pool = pool(100);
        assertEquals(0, pool.insert("ferro", 0, false));
        assertEquals(0, pool.insert("ferro", -5, false));
        assertEquals(0, pool.extract("ferro", -5, false));
        assertFalse(pool.hasChanges());
    }

    @Test
    void drainChangesReportsCurrentAmountsOnceIncludingZero() {
        ResourcePool<String> pool = pool(100);
        pool.insert("ferro", 10, false);
        pool.insert("ouro", 5, false);
        pool.insert("ferro", 5, false);
        pool.extract("ouro", 5, false);
        Map<String, Long> seen = new HashMap<>();
        pool.drainChanges(seen::put);
        assertEquals(Map.of("ferro", 15L, "ouro", 0L), seen);
        assertFalse(pool.hasChanges());
        seen.clear();
        pool.drainChanges(seen::put);
        assertTrue(seen.isEmpty(), "já foi entregue");
    }

    @Test
    void snapshotIsIndependentFromThePool() {
        ResourcePool<String> pool = pool(100);
        pool.insert("ferro", 10, false);
        var copy = pool.snapshot();
        pool.insert("ferro", 10, false);
        assertEquals(10, copy.getLong("ferro"));
        assertEquals(0, copy.getLong("nada"));
    }
}
