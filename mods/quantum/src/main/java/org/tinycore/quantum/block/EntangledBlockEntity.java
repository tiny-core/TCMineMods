package org.tinycore.quantum.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.Config;
import org.tinycore.quantum.grid.BlockLocation;
import org.tinycore.quantum.grid.MemberKind;
import org.tinycore.quantum.grid.NetworkMember;
import org.tinycore.quantum.grid.NetworkRecord;
import org.tinycore.quantum.grid.QuantumNetworks;

import java.util.UUID;

/**
 * Base dos block entities que entram numa rede quântica (Matriz agora; Âncoras, Interface e Bateria depois).
 *
 * <p>Guarda o id da rede no bloco e se registra como membro na rede ({@link QuantumNetworks}). A rede é a fonte da
 * verdade para capacidade e linhas de partículas; o id no bloco serve para o bloco saber a quem pertence e para
 * recolocar o membro se a rede perder o registro (por exemplo, o mundo gravou o chunk mas não o arquivo das redes).
 *
 * <p>Quem herda diz o tipo ({@link #memberKind()}) e quanto soma de capacidade ({@link #memberCapacity()}).
 */
public abstract class EntangledBlockEntity extends BlockEntity {

    private static final String TAG_NETWORK = "network";

    private @Nullable UUID networkId;

    protected EntangledBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract MemberKind memberKind();

    protected abstract long memberCapacity();

    public @Nullable UUID networkId() {
        return networkId;
    }

    /**
     * Entra na rede {@code id} (saindo da anterior, se houver).
     *
     * @return {@code false} se a rede não existe
     */
    public boolean entangle(ServerLevel level, UUID id) {
        QuantumNetworks networks = QuantumNetworks.get(level);
        if (networks.get(id) == null) {
            return false;
        }
        if (networkId != null && !networkId.equals(id)) {
            networks.removeMember(networkId, location(level));
        }
        networkId = id;
        networks.addMember(id, new NetworkMember(location(level), memberKind(), memberCapacity()));
        setChanged();
        return true;
    }

    /** Sai da rede (Entrelaçador com Shift). */
    public void disentangle(ServerLevel level) {
        if (networkId == null) {
            return;
        }
        QuantumNetworks.get(level).removeMember(networkId, location(level));
        networkId = null;
        setChanged();
    }

    /**
     * Colocado perto de um Núcleo: entra sozinho na rede dele ({@code autoEntangleRadius}).
     *
     * @return a rede assumida, ou {@code null} se não há Núcleo perto (ou o recurso está desligado)
     */
    public @Nullable NetworkRecord autoEntangle(ServerLevel level) {
        int radius = Config.AUTO_ENTANGLE_RADIUS.get();
        if (radius <= 0 || networkId != null) {
            return null;
        }
        NetworkRecord nearest = QuantumNetworks.get(level)
                .nearestCore(level.dimension().location().toString(), worldPosition, radius);
        return nearest != null && entangle(level, nearest.id()) ? nearest : null;
    }

    /**
     * O tipo ou a capacidade deste bloco mudou (módulo encaixado ou tirado): atualiza a vaga dele na rede, que
     * recalcula a capacidade dos pools.
     */
    protected void refreshMembership() {
        if (networkId != null && level instanceof ServerLevel server) {
            QuantumNetworks.get(server).addMember(networkId,
                    new NetworkMember(location(server), memberKind(), memberCapacity()));
        }
    }

    /** Rede deste bloco, se estiver entrelaçado e a rede existir. Só no servidor. */
    public @Nullable NetworkRecord network() {
        if (networkId == null || !(level instanceof ServerLevel server)) {
            return null;
        }
        return QuantumNetworks.get(server).get(networkId);
    }

    /** O bloco saiu do mundo: deixa de contar na rede. */
    public void onRemoved(ServerLevel level) {
        if (networkId != null) {
            QuantumNetworks.get(level).removeMember(networkId, location(level));
        }
    }

    /**
     * Chamado pelo NeoForge quando o block entity entra no mundo (colocado ou chunk carregado). Recoloca o membro na
     * rede se ele sumiu de lá; se a rede não existe mais, o bloco fica sem rede.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (networkId == null || !(level instanceof ServerLevel server)) {
            return;
        }
        if (!QuantumNetworks.get(server).addMember(networkId,
                new NetworkMember(location(server), memberKind(), memberCapacity()))) {
            networkId = null;
            setChanged();
        }
    }

    protected BlockLocation location(ServerLevel level) {
        return new BlockLocation(level.dimension().location().toString(), worldPosition.asLong());
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (networkId != null) {
            tag.putUUID(TAG_NETWORK, networkId);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        networkId = tag.hasUUID(TAG_NETWORK) ? tag.getUUID(TAG_NETWORK) : null;
    }
}
