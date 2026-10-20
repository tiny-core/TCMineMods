package org.tinycore.quantum.grid;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Uma rede quântica guardada no mundo: o id, a energia e onde está o Núcleo (ou {@code null} quando o Núcleo foi
 * quebrado e está no inventário de alguém). O conteúdo da rede fica aqui, não no bloco: quebrar e recolocar o
 * Núcleo (o item guarda o id) devolve a mesma rede. Os pools de itens e fluidos entram aqui na fatia da Matriz.
 */
public final class NetworkRecord {

    private final UUID id;
    private final NetworkPower power;
    private @Nullable CoreLocation core;

    public NetworkRecord(UUID id, NetworkPower power, @Nullable CoreLocation core) {
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

    public @Nullable CoreLocation core() {
        return core;
    }

    void attach(CoreLocation location) {
        this.core = location;
    }

    void detach() {
        this.core = null;
    }
}
