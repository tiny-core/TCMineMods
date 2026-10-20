package org.tinycore.quantum.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bloco que pode ser entrelaçado com uma rede. Diz onde está o block entity ({@link EntangledBlockEntity}) a partir
 * de qualquer parte do bloco: a Matriz tem duas metades e só a de baixo tem block entity.
 *
 * <p>{@code interface} em Java ≈ {@code interface} em C#. O Entrelaçador só conhece esta interface, nunca os blocos
 * concretos: um bloco novo entra na rede implementando-a.
 */
public interface EntangledBlock {

    /** Posição do block entity para um clique em {@code pos}. */
    BlockPos entanglementPos(BlockPos pos, BlockState state);
}
