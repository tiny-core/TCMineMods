package org.tinycore.quantum.block.matrix;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.tinycore.quantum.Config;
import org.tinycore.quantum.block.EntangledBlockEntity;
import org.tinycore.quantum.grid.MemberKind;
import org.tinycore.quantum.registry.ModBlockEntities;

/**
 * Block entity da {@link SuperpositionMatrixBlock} (metade de baixo). Diz à rede que é uma Matriz de itens e quanto
 * soma de capacidade: a base da config mais os Módulos de Qubits encaixados ({@link MatrixModules}).
 *
 * <p>Os módulos são a única coisa guardada no bloco: o conteúdo é da rede.
 */
public class SuperpositionMatrixBlockEntity extends EntangledBlockEntity {

    private static final String TAG_MODULES = "modules";

    private final MatrixModules modules = new MatrixModules();

    public SuperpositionMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUPERPOSITION_MATRIX.get(), pos, state);
    }

    public MatrixModules modules() {
        return modules;
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
    }
}
