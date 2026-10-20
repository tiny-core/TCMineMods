package org.tinycore.quantum.block.matrix;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.tinycore.quantum.Config;
import org.tinycore.quantum.block.EntangledBlockEntity;
import org.tinycore.quantum.grid.MemberKind;
import org.tinycore.quantum.grid.NetworkRecord;
import org.tinycore.quantum.storage.ResourcePool;
import org.tinycore.quantum.registry.ModBlockEntities;

/**
 * Block entity da {@link SuperpositionMatrixBlock} (metade de baixo). Diz à rede que é uma Matriz de itens e quanto
 * soma de capacidade: a base da config mais os Módulos de Qubits encaixados ({@link MatrixModules}).
 *
 * <p>Os módulos são a única coisa guardada no bloco: o conteúdo é da rede.
 *
 * <p><b>Efeitos no cliente:</b> o renderizador ({@code client/render/MatrixEffectsRenderer}) precisa saber se a rede
 * está ligada e quanto ela está cheia. Uma vez por segundo ({@link #serverTick}) o servidor confere esses dois valores
 * e, só se mudaram, manda o bloco atualizado ao cliente ({@code getUpdateTag}). Os módulos vão junto.
 */
public class SuperpositionMatrixBlockEntity extends EntangledBlockEntity {

    private static final String TAG_MODULES = "modules";
    private static final String TAG_ONLINE = "visualOnline";
    private static final String TAG_FILL = "visualFill";
    private static final int SYNC_INTERVAL_TICKS = 20;

    private final MatrixModules modules = new MatrixModules();
    /** O que o cliente desenha (no servidor: o último valor enviado). */
    private boolean visualOnline;
    private int visualFill;

    public SuperpositionMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUPERPOSITION_MATRIX.get(), pos, state);
    }

    public MatrixModules modules() {
        return modules;
    }

    /** Rede ligada (com energia e Núcleo), para os efeitos. No cliente, o último valor recebido. */
    public boolean visualOnline() {
        return visualOnline;
    }

    /** Ocupação da rede de itens em % (0 a 100), para os medidores. No cliente, o último valor recebido. */
    public int visualFill() {
        return visualFill;
    }

    /** Uma vez por segundo: se o estado visível mudou, manda ao cliente. */
    public void serverTick() {
        if (level == null || level.getGameTime() % SYNC_INTERVAL_TICKS != 0) {
            return;
        }
        NetworkRecord network = network();
        boolean online = network != null && network.core() != null && network.power().online();
        int fill = 0;
        if (network != null) {
            ResourcePool<?> items = network.storage().items();
            fill = GaugeLevels.percent(items.stored(), items.capacity());
        }
        if (online != visualOnline || fill != visualFill) {
            visualOnline = online;
            visualFill = fill;
            syncToClient();
        }
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Encaixa um módulo e atualiza a capacidade da rede.
     *
     * @return {@code false} se a Matriz já está cheia
     */
    public boolean insertModule(int tier) {
        if (!modules.insert(tier)) {
            return false;
        }
        onModulesChanged();
        return true;
    }

    /**
     * Tira o último módulo e atualiza a capacidade da rede.
     *
     * @return o nível do módulo tirado, ou 0 se não havia nenhum
     */
    public int removeModule() {
        int tier = modules.removeLast();
        if (tier != 0) {
            onModulesChanged();
        }
        return tier;
    }

    private void onModulesChanged() {
        setChanged();
        refreshMembership();
        syncToClient();
    }

    @Override
    protected MemberKind memberKind() {
        return MemberKind.ITEM_MATRIX;
    }

    @Override
    public long memberCapacity() {
        return modules.capacity(Config.MATRIX_ITEM_CAPACITY.get(), Config.qubitModuleCapacities());
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray(TAG_MODULES, modules.toArray());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        modules.load(tag.getIntArray(TAG_MODULES));
        if (tag.contains(TAG_FILL)) { // só vem na sincronização com o cliente
            visualOnline = tag.getBoolean(TAG_ONLINE);
            visualFill = tag.getInt(TAG_FILL);
        }
    }

    /** Dados para o cliente (chunk carregando ou {@code sendBlockUpdated}): módulos e o estado visível. */
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = saveCustomOnly(registries);
        tag.putBoolean(TAG_ONLINE, visualOnline);
        tag.putInt(TAG_FILL, visualFill);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
