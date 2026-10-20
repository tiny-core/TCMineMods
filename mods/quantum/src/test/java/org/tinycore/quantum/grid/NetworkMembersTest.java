package org.tinycore.quantum.grid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link NetworkMembers}: uma vaga por bloco e capacidade somada por tipo, sem estourar. */
class NetworkMembersTest {

    private static final BlockLocation A = new BlockLocation("minecraft:overworld", 1);
    private static final BlockLocation B = new BlockLocation("minecraft:overworld", 2);

    @Test
    void capacityIsTheSumOfTheMembersOfAKind() {
        NetworkMembers members = new NetworkMembers();
        members.put(new NetworkMember(A, MemberKind.ITEM_MATRIX, 1_000));
        members.put(new NetworkMember(B, MemberKind.ITEM_MATRIX, 500));
        assertEquals(2, members.count(MemberKind.ITEM_MATRIX));
        assertEquals(1_500, members.capacity(MemberKind.ITEM_MATRIX));
    }

    @Test
    void sameBlockTakesOneSlotAndUpdates() {
        NetworkMembers members = new NetworkMembers();
        assertTrue(members.put(new NetworkMember(A, MemberKind.ITEM_MATRIX, 1_000)));
        assertFalse(members.put(new NetworkMember(A, MemberKind.ITEM_MATRIX, 1_000)), "nada mudou");
        assertTrue(members.put(new NetworkMember(A, MemberKind.ITEM_MATRIX, 4_000)), "capacidade nova");
        assertEquals(1, members.size());
        assertEquals(4_000, members.capacity(MemberKind.ITEM_MATRIX));
    }

    @Test
    void removeOnlyWhatIsThere() {
        NetworkMembers members = new NetworkMembers();
        members.put(new NetworkMember(A, MemberKind.ITEM_MATRIX, 1_000));
        assertFalse(members.remove(B));
        assertTrue(members.remove(A));
        assertFalse(members.contains(A));
        assertEquals(0, members.capacity(MemberKind.ITEM_MATRIX));
    }

    @Test
    void capacitySaturatesInsteadOfOverflowing() {
        NetworkMembers members = new NetworkMembers();
        members.put(new NetworkMember(A, MemberKind.ITEM_MATRIX, Long.MAX_VALUE));
        members.put(new NetworkMember(B, MemberKind.ITEM_MATRIX, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, members.capacity(MemberKind.ITEM_MATRIX));
    }
}
