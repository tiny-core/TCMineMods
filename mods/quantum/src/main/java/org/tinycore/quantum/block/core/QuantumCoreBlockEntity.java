package org.tinycore.quantum.block.core;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.Config;
import org.tinycore.quantum.grid.BlockLocation;
import org.tinycore.quantum.grid.MemberKind;
import org.tinycore.quantum.grid.NetworkMembers;
import org.tinycore.quantum.grid.NetworkPower;
import org.tinycore.quantum.grid.NetworkRecord;
import org.tinycore.quantum.grid.QuantumNetworks;
import org.tinycore.quantum.registry.ModBlockEntities;
import org.tinycore.quantum.registry.ModDataComponents;
import org.tinycore.quantum.storage.QuantityFormat;

import java.util.UUID;

/**
 * Block entity do centro do {@link QuantumCoreBlock}. Liga o bloco a uma rede de {@link QuantumNetworks} e, a cada
 * tick, cobra o consumo de energia da rede ({@link NetworkPower}).
 *
 * <p>O bloco guarda só o <b>id</b> da rede; energia e conteúdo ficam na rede (no mundo). Ao quebrar, a loot table
 * copia o id para o item ({@link #collectImplicitComponents}); ao colocar, o item devolve o id
 * ({@link #applyImplicitComponents}). Assim mudar o Núcleo de lugar nunca perde nada.
 *
 * <p>A energia entra por {@link CoreEnergyStorage}, exposta como capability em todo o cubo ({@code ModCapabilities}).
 */
public class QuantumCoreBlockEntity extends BlockEntity {

    private static final String TAG_NETWORK = "network";

    private final CoreEnergyStorage energy = new CoreEnergyStorage(this);
    private @Nullable UUID networkId;
    /** Cache da rede e do registro (só no servidor); refeito se o bloco for recarregado. */
    private @Nullable NetworkRecord network;
    private @Nullable QuantumNetworks networks;

    public QuantumCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_CORE.get(), pos, state);
    }

    public IEnergyStorage energyStorage() {
        return energy;
    }

    /** Capacidade de energia da rede (hoje só a do Núcleo; as Baterias somam na fase 2). */
    static long energyCapacity() {
        return Config.CORE_ENERGY_CAPACITY.get();
    }

    /**
     * Block entity do Núcleo a partir de qualquer um dos 27 blocos do cubo (o Entrelaçador clica em qualquer parte).
     */
    public static @Nullable QuantumCoreBlockEntity at(Level level, BlockPos pos, BlockState state) {
        BlockPos center = state.getBlock() instanceof CoreStructureBlock ? CoreStructureBlock.centerOf(pos, state) : pos;
        return level.getBlockEntity(center) instanceof QuantumCoreBlockEntity core ? core : null;
    }

    /** Id da rede deste Núcleo (liga à rede se ainda não ligou). {@code null} no cliente. */
    public @Nullable UUID boundNetworkId() {
        NetworkRecord record = network();
        return record == null ? null : record.id();
    }

    // ---------------------------------------------------------------- rede

    /** Rede deste Núcleo, ligando na primeira chamada. {@code null} no cliente. */
    @Nullable NetworkRecord network() {
        if (network == null && level instanceof ServerLevel server) {
            bind(server);
        }
        return network;
    }

    /** Colocado por um jogador: assume a rede do item (se houver) já na colocação. */
    void bindOnPlacement(ServerLevel server, ItemStack stack) {
        UUID fromItem = stack.get(ModDataComponents.CORE_NETWORK.get());
        if (fromItem != null) {
            networkId = fromItem;
        }
        bind(server);
    }

    private void bind(ServerLevel server) {
        networks = QuantumNetworks.get(server);
        NetworkRecord record = networks.claim(networkId, location(server));
        if (!record.id().equals(networkId)) {
            networkId = record.id();
            setChanged();
        }
        network = record;
    }

    /** O bloco saiu do mundo: a rede fica guardada, sem Núcleo, esperando o item ser colocado de novo. */
    void onRemoved(ServerLevel server) {
        if (networkId != null) {
            QuantumNetworks.get(server).detach(networkId, location(server));
        }
        network = null;
    }

    private BlockLocation location(ServerLevel server) {
        return new BlockLocation(server.dimension().location().toString(), worldPosition.asLong());
    }

    /** A energia da rede mudou: o arquivo das redes precisa ser gravado no próximo save. */
    void markNetworksDirty() {
        if (networks != null) {
            networks.setDirty();
        }
    }

    // ---------------------------------------------------------------- tick

    public void serverTick() {
        NetworkRecord record = network();
        if (record == null) {
            return;
        }
        long cost = Config.CORE_ENERGY_PER_TICK.get();
        NetworkPower power = record.power();
        power.tick(cost, Config.STANDBY_RESUME_TICKS.get());
        if (cost > 0 && power.online()) {
            markNetworksDirty();
        }
        BlockState state = getBlockState();
        if (state.getValue(QuantumCoreBlock.ONLINE) != power.online() && level != null) {
            level.setBlock(worldPosition, state.setValue(QuantumCoreBlock.ONLINE, power.online()), Block.UPDATE_ALL);
        }
    }

    // ---------------------------------------------------------------- estado para o jogador

    /** Mensagem no chat com o estado da rede (até o Terminal ter a aba Rede). */
    void sendStatus(Player player) {
        NetworkRecord record = network();
        if (record == null) {
            return;
        }
        NetworkPower power = record.power();
        Component state = power.online()
                ? Component.translatable("message.tcquantum.core.online").withStyle(ChatFormatting.AQUA)
                : Component.translatable("message.tcquantum.core.standby").withStyle(ChatFormatting.GOLD);
        NetworkMembers members = record.members();
        player.displayClientMessage(Component.translatable("message.tcquantum.core.status",
                state,
                QuantityFormat.compact(power.stored()),
                QuantityFormat.compact(energyCapacity()),
                QuantityFormat.compact(Config.CORE_ENERGY_PER_TICK.get()),
                shortId(record.id())), false);
        player.displayClientMessage(Component.translatable("message.tcquantum.core.members",
                members.count(MemberKind.ITEM_MATRIX),
                QuantityFormat.compact(members.capacity(MemberKind.ITEM_MATRIX))), false);
    }

    /** Primeiros 8 caracteres do id: o bastante para o jogador diferenciar as redes. */
    public static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    // ---------------------------------------------------------------- disco e item

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
        network = null;
    }

    /** Item → bloco: o id da rede que o item trazia. */
    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        UUID fromItem = input.get(ModDataComponents.CORE_NETWORK.get());
        if (fromItem != null) {
            networkId = fromItem;
        }
    }

    /** Bloco → item (loot table {@code copy_components}): o id da rede vai junto. */
    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (networkId != null) {
            builder.set(ModDataComponents.CORE_NETWORK.get(), networkId);
        }
    }

    /** O id já vai como componente; não duplicar no NBT do item. */
    @Override
    public void removeComponentsFromTag(@NotNull CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(TAG_NETWORK);
    }
}
