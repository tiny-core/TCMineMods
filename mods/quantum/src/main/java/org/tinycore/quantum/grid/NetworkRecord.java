package org.tinycore.quantum.grid;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Uma rede quântica guardada no mundo: o id, a energia, os blocos entrelaçados ({@link NetworkMembers}) e onde está
 * o Núcleo (ou {@code null} quando o Núcleo foi quebrado e está no inventário de alguém). O conteúdo da rede fica
 * aqui ({@link NetworkStorage}), não no bloco: quebrar e recolocar o Núcleo (o item guarda o id) devolve a mesma
 * rede, com tudo dentro.
 */
public final class NetworkRecord {

    private final UUID id;
    private final NetworkPower power;
    private final NetworkMembers members = new NetworkMembers();
    private final NetworkStorage storage = new NetworkStorage();
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

    public NetworkStorage storage() {
        return storage;
    }

    /**
     * Ajusta a capacidade de cada pool à soma dos membros (Matrizes). Chamado quando um membro entra, sai ou muda, e
     * ao carregar. Se a capacidade cair abaixo do que está guardado, nada se perde: o pool só deixa de aceitar mais.
     */
    public void refreshCapacities() {
        storage.items().setCapacity(members.capacity(MemberKind.ITEM_MATRIX));
        // Fluidos: capacidade 0 até existir a Matriz de Fluidos.
        storage.fluids().setCapacity(0);
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
