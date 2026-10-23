package org.tinycore.quantum.block.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link MatrixModules}: 5 encaixes, entra no primeiro vazio, sai o último, capacidade somada sem estourar. */
class MatrixModulesTest {

    private static final long[] PER_TIER = {100, 1_000, 10_000, 100_000, 1_000_000};

    @Test
    void emptyMatrixHasTheBaseCapacity() {
        assertEquals(50, new MatrixModules().capacity(50, PER_TIER));
    }

    @Test
    void modulesAddTheirTierCapacity() {
        MatrixModules modules = new MatrixModules();
        assertTrue(modules.insert(1));
        assertTrue(modules.insert(5));
        assertEquals(50 + 100 + 1_000_000, modules.capacity(50, PER_TIER));
        assertEquals(2, modules.count());
    }

    @Test
    void fullMatrixRejectsAFifthModule() {
        MatrixModules modules = new MatrixModules();
        for (int i = 0; i < MatrixModules.SLOTS; i++) {
            assertTrue(modules.insert(2));
        }
        assertTrue(modules.isFull());
        assertFalse(modules.insert(2));
    }

    @Test
    void invalidTiersAreRejected() {
        MatrixModules modules = new MatrixModules();
        assertFalse(modules.insert(0));
        assertFalse(modules.insert(MatrixModules.MAX_TIER + 1));
        assertEquals(0, modules.count());
    }

    @Test
    void removesTheLastInstalledModule() {
        MatrixModules modules = new MatrixModules();
        modules.insert(1);
        modules.insert(3);
        assertEquals(3, modules.removeLast());
        assertEquals(1, modules.removeLast());
        assertEquals(0, modules.removeLast(), "vazia");
    }

    @Test
    void capacitySaturates() {
        MatrixModules modules = new MatrixModules();
        modules.insert(5);
        modules.insert(5);
        assertEquals(Long.MAX_VALUE, modules.capacity(Long.MAX_VALUE, PER_TIER));
    }

    @Test
    void loadIgnoresInvalidData() {
        MatrixModules modules = new MatrixModules();
        modules.load(new int[]{2, 9, -1, 4, 0, 5});
        assertEquals(2, modules.count(), "9 e -1 são inválidos; encaixes a mais são ignorados");
        assertEquals(4, modules.removeLast());
        assertEquals(2, modules.removeLast());
    }
}
