package org.tinycore.quantum.block.matrix;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.tinycore.quantum.Config;
import org.tinycore.quantum.block.EntangledBlockEntity;
import org.tinycore.quantum.grid.MemberKind;
import org.tinycore.quantum.registry.ModBlockEntities;

/**
 * Block entity da {@link SuperpositionMatrixBlock} (metade de baixo). Por enquanto só diz à rede que é uma Matriz de
 * itens com a capacidade base da config; os Módulos de Qubits (fatia 1.4) aumentam {@link #memberCapacity()}.
 */
public class SuperpositionMatrixBlockEntity extends EntangledBlockEntity {

    public SuperpositionMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUPERPOSITION_MATRIX.get(), pos, state);
    }

    @Override
    protected MemberKind memberKind() {
        return MemberKind.ITEM_MATRIX;
    }

    @Override
    protected long memberCapacity() {
        return Config.MATRIX_ITEM_CAPACITY.get();
    }
}
