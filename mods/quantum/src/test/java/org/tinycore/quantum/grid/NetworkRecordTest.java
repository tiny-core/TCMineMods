package org.tinycore.quantum.grid;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** {@link NetworkRecord#refreshCapacities}: a capacidade de itens é a soma das Matrizes; fluidos ficam em 0. */
class NetworkRecordTest {

    private static final BlockLocation A = new BlockLocation("minecraft:overworld", 1);
    private static final BlockLocation B = new BlockLocation("minecraft:overworld", 2);

    @Test
    void itemCapacityFollowsTheMatrices() {
        NetworkRecord record = new NetworkRecord(UUID.randomUUID(), new NetworkPower(0), null);
        record.members().put(new NetworkMember(A, MemberKind.ITEM_MATRIX, 1_000));
        record.members().put(new NetworkMember(B, MemberKind.ITEM_MATRIX, 3_000));
        record.refreshCapacities();
        assertEquals(4_000, record.storage().items().capacity());
        assertEquals(0, record.storage().fluids().capacity(), "sem Matriz de Fluidos ainda");

        record.members().remove(A);
        record.refreshCapacities();
        assertEquals(3_000, record.storage().items().capacity());
    }
}
