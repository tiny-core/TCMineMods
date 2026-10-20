package org.tinycore.quantum.block.core;

import org.junit.jupiter.api.Test;
import org.tinycore.quantum.block.core.CoreFootprint.Offset;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link CoreFootprint}: o cubo 3×3×3 e a ida e volta entre deslocamento e propriedades do bloco. */
class CoreFootprintTest {

    @Test
    void cubeHas27DistinctPositionsAnd26StructureBlocks() {
        assertEquals(27, new HashSet<>(CoreFootprint.all()).size());
        assertEquals(26, CoreFootprint.structure().size());
        assertTrue(CoreFootprint.structure().stream().noneMatch(Offset::isCenter), "o centro é o bloco principal");
    }

    @Test
    void everyOffsetIsWithinOneBlockOfTheCenter() {
        for (Offset o : CoreFootprint.all()) {
            assertTrue(Math.abs(o.dx()) <= 1 && Math.abs(o.dy()) <= 1 && Math.abs(o.dz()) <= 1, o.toString());
        }
    }

    @Test
    void propertyIndicesRoundTrip() {
        for (Offset o : CoreFootprint.all()) {
            assertTrue(o.indexX() >= 0 && o.indexX() <= CoreFootprint.MAX_INDEX);
            assertTrue(o.indexY() >= 0 && o.indexY() <= CoreFootprint.MAX_INDEX);
            assertTrue(o.indexZ() >= 0 && o.indexZ() <= CoreFootprint.MAX_INDEX);
            assertEquals(o, Offset.fromIndices(o.indexX(), o.indexY(), o.indexZ()));
        }
    }

    @Test
    void centerIndicesPointToTheCenter() {
        Offset center = Offset.fromIndices(CoreFootprint.CENTER, CoreFootprint.CENTER, CoreFootprint.CENTER);
        assertTrue(center.isCenter());
        assertFalse(Offset.fromIndices(0, 1, 1).isCenter());
    }
}
