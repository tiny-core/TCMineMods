package org.tinycore.quantum.grid;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Uma rede quântica guardada no mundo: o id, a energia, os blocos entrelaçados ({@link NetworkMembers}) e onde está
 * o Núcleo (ou {@code null} quando o Núcleo foi quebrado e está no inventário de alguém). O conteúdo da rede fica
 * aqui, não no bloco: quebrar e recolocar o Núcleo (o item guarda o id) devolve a mesma rede. Os pools de itens e
 * fluidos entram aqui na fatia 1.4.
 */
public final class NetworkRecord {

    private final UUID id;
    private final NetworkPower power;
    private final NetworkMembers members = new NetworkMembers();
    private @Nullable BlockLocation core;

    public NetworkRecord(UUID id, NetworkPower power, @Nullable BlockLocation core) {
        this.id = id;
        this.power = power;
        this.core = core;
    }

    public UUID id() {
        return id;
    }

    public NetworkPower power() {
        return power;
    }

    public NetworkMembers members() {
        return members;
    }

    public @Nullable BlockLocation core() {
        return core;
    }

    void attach(BlockLocation location) {
        this.core = location;
    }

    void detach() {
        this.core = null;
    }
}
