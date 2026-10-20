package org.tinycore.quantum.grid;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link NetworkDirectory}: qual rede um Núcleo colocado assume, e o que acontece quando ele sai do mundo. */
class NetworkDirectoryTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID C = UUID.fromString("00000000-0000-0000-0000-00000000000c");
    private static final CoreLocation HERE = new CoreLocation("minecraft:overworld", 10);
    private static final CoreLocation THERE = new CoreLocation("minecraft:the_nether", 10);

    private static Supplier<UUID> ids(UUID... values) {
        Deque<UUID> queue = new ArrayDeque<>(List.of(values));
        return queue::pop;
    }

    @Test
    void newCoreCreatesANewNetwork() {
        NetworkDirectory directory = new NetworkDirectory();
        NetworkRecord record = directory.claim(null, HERE, ids(A));
        assertEquals(A, record.id());
        assertEquals(HERE, record.core());
        assertSame(record, directory.get(A));
    }

    @Test
    void replacedCoreGetsItsNetworkBack() {
        NetworkDirectory directory = new NetworkDirectory();
        NetworkRecord first = directory.claim(null, HERE, ids(A));
        first.power().receive(500, 1_000, false);
        assertTrue(directory.detach(A, HERE));
        assertNull(first.core(), "rede guardada sem Núcleo");

        NetworkRecord again = directory.claim(A, THERE, ids(B));
        assertSame(first, again, "mesma rede, mesmo conteúdo");
        assertEquals(500, again.power().stored());
        assertEquals(THERE, again.core());
    }

    @Test
    void copiedCoreNeverSharesANetwork() {
        NetworkDirectory directory = new NetworkDirectory();
        directory.claim(null, HERE, ids(A));
        NetworkRecord copy = directory.claim(A, THERE, ids(B));
        assertNotEquals(A, copy.id(), "a rede A já tem Núcleo em outro lugar");
        assertEquals(HERE, directory.get(A).core(), "o Núcleo original continua com a rede dele");
    }

    @Test
    void sameCoreReloadingKeepsItsNetwork() {
        NetworkDirectory directory = new NetworkDirectory();
        directory.claim(null, HERE, ids(A));
        assertEquals(A, directory.claim(A, HERE, ids(B)).id());
    }

    @Test
    void unknownIdStartsEmptyWithTheSameId() {
        NetworkDirectory directory = new NetworkDirectory();
        NetworkRecord record = directory.claim(C, HERE, ids(A));
        assertEquals(C, record.id());
        assertEquals(0, record.power().stored());
    }

    @Test
    void generatedIdNeverCollides() {
        NetworkDirectory directory = new NetworkDirectory();
        directory.claim(null, HERE, ids(A));
        assertEquals(B, directory.claim(null, THERE, ids(A, B)).id());
    }

    @Test
    void detachOnlyFromTheCoreThatOwnsTheNetwork() {
        NetworkDirectory directory = new NetworkDirectory();
        directory.claim(null, HERE, ids(A));
        assertFalse(directory.detach(A, THERE));
        assertFalse(directory.detach(B, HERE));
        assertEquals(HERE, directory.get(A).core());
    }
}
