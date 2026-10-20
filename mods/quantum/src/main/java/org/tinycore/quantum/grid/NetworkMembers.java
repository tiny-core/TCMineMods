package org.tinycore.quantum.grid;

import org.tinycore.quantum.storage.Amounts;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Os blocos entrelaçados de uma rede, por posição. Regra pura: um bloco ocupa uma vaga só (entrar de novo
 * atualiza a vaga), e a capacidade total de um tipo é a soma das capacidades dos membros desse tipo, saturada em
 * {@link Amounts#MAX}.
 *
 * <p>{@code LinkedHashMap} ≈ {@code Dictionary} do C# que lembra a ordem de inserção: as linhas de partículas e as
 * listas aparecem sempre na mesma ordem.
 */
public final class NetworkMembers {

    private final Map<BlockLocation, NetworkMember> members = new LinkedHashMap<>();

    /**
     * Acrescenta ou atualiza um membro.
     *
     * @return {@code true} se algo mudou (para quem grava no disco)
     */
    public boolean put(NetworkMember member) {
        NetworkMember previous = members.put(member.location(), member);
        return !member.equals(previous);
    }

    /** @return {@code true} se o bloco era membro */
    public boolean remove(BlockLocation location) {
        return members.remove(location) != null;
    }

    public boolean contains(BlockLocation location) {
        return members.containsKey(location);
    }

    public int count(MemberKind kind) {
        int count = 0;
        for (NetworkMember member : members.values()) {
            if (member.kind() == kind) {
                count++;
            }
        }
        return count;
    }

    /** Capacidade total dos membros de um tipo. */
    public long capacity(MemberKind kind) {
        long total = 0;
        for (NetworkMember member : members.values()) {
            if (member.kind() == kind) {
                total = Amounts.add(total, member.capacity());
            }
        }
        return total;
    }

    public int size() {
        return members.size();
    }

    public Collection<NetworkMember> all() {
        return Collections.unmodifiableCollection(members.values());
    }
}
