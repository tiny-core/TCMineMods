package org.tinycore.quantum.grid;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Todas as redes do servidor, por id. Regra pura: decide qual rede um Núcleo recém-colocado assume. Quem guarda
 * isto no mundo é o {@link QuantumNetworks} ({@code SavedData}).
 *
 * <p>Regra de {@link #claim}: o Núcleo assume a rede do id que o item trazia, se ela estiver livre (sem Núcleo) ou
 * já for dele. Se outro Núcleo já ocupa essa rede (item copiado no criativo, por exemplo), cria uma rede nova:
 * duas cópias nunca compartilham o mesmo conteúdo.
 */
public final class NetworkDirectory {

    private final Map<UUID, NetworkRecord> records = new HashMap<>();

    public @Nullable NetworkRecord get(UUID id) {
        return records.get(id);
    }

    public Collection<NetworkRecord> records() {
        return Collections.unmodifiableCollection(records.values());
    }

    /** Acrescenta uma rede lida do disco. */
    public void put(NetworkRecord record) {
        records.put(record.id(), record);
    }

    /**
     * Liga um Núcleo colocado em {@code at} a uma rede.
     *
     * @param requested id que o item do Núcleo trazia (ou {@code null} se é um Núcleo novo)
     * @param newIds    gerador de ids (no jogo, {@code UUID::randomUUID}; nos testes, ids fixos)
     * @return a rede assumida (existente ou nova)
     */
    public NetworkRecord claim(@Nullable UUID requested, CoreLocation at, Supplier<UUID> newIds) {
        if (requested != null) {
            NetworkRecord existing = records.get(requested);
            if (existing == null) {
                // Rede que este servidor não conhece (item vindo de outro mundo): começa vazia, com o mesmo id.
                return create(requested, at);
            }
            if (existing.core() == null || existing.core().equals(at)) {
                existing.attach(at);
                return existing;
            }
        }
        UUID id = newIds.get();
        while (records.containsKey(id)) {
            id = newIds.get();
        }
        return create(id, at);
    }

    /**
     * O Núcleo em {@code at} saiu do mundo: a rede fica guardada, sem Núcleo, até o item ser colocado de novo.
     *
     * @return {@code true} se a rede estava mesmo ligada a esse Núcleo
     */
    public boolean detach(UUID id, CoreLocation at) {
        NetworkRecord record = records.get(id);
        if (record == null || !at.equals(record.core())) {
            return false;
        }
        record.detach();
        return true;
    }

    private NetworkRecord create(UUID id, CoreLocation at) {
        NetworkRecord record = new NetworkRecord(id, new NetworkPower(0), at);
        records.put(id, record);
        return record;
    }
}
