package org.tinycore.quantum.block.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.tinycore.quantum.block.core.CoreFootprint.Offset;
import org.tinycore.quantum.registry.ModBlocks;
import org.tinycore.quantum.registry.ModItems;

/**
 * Um dos 26 blocos invisíveis que formam o cubo do {@link QuantumCoreBlock} em volta do centro. Existe para o
 * Núcleo ter colisão e receber cliques, quebra e cabos em todo o volume (o jogo só testa cada bloco dentro do
 * próprio cubo de 1×1×1).
 *
 * <p>A posição no cubo fica nas propriedades {@link #OFFSET_X}/{@link #OFFSET_Y}/{@link #OFFSET_Z} (0 a 2, ver
 * {@link CoreFootprint}), então o centro é achado sem block entity ({@link #centerOf}).
 *
 * <p>Nunca derruba item (sem loot table). Quebrar este bloco quebra o centro, que derruba o item do Núcleo uma
 * vez só, com a rede guardada nele. No criativo, quebra o centro sem derrubar nada (como a porta do vanilla).
 */
public class CoreStructureBlock extends Block {

    public static final IntegerProperty OFFSET_X = IntegerProperty.create("ox", 0, CoreFootprint.MAX_INDEX);
    public static final IntegerProperty OFFSET_Y = IntegerProperty.create("oy", 0, CoreFootprint.MAX_INDEX);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("oz", 0, CoreFootprint.MAX_INDEX);

    /** Contorno do cubo inteiro visto de cada posição (índice = x·9 + y·3 + z), calculado uma vez. */
    private static final VoxelShape[] SHAPES = new VoxelShape[27];

    static {
        for (Offset o : CoreFootprint.all()) {
            SHAPES[shapeIndex(o.indexX(), o.indexY(), o.indexZ())] =
                    QuantumCoreBlock.CUBE.move(-o.dx(), -o.dy(), -o.dz());
        }
    }

    public CoreStructureBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(OFFSET_X, CoreFootprint.CENTER)
                .setValue(OFFSET_Y, CoreFootprint.CENTER)
                .setValue(OFFSET_Z, CoreFootprint.CENTER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    /** Estado do bloco de estrutura para uma posição do cubo. */
    public static BlockState stateFor(Offset offset) {
        return ModBlocks.CORE_STRUCTURE.get().defaultBlockState()
                .setValue(OFFSET_X, offset.indexX())
                .setValue(OFFSET_Y, offset.indexY())
                .setValue(OFFSET_Z, offset.indexZ());
    }

    /** Posição do centro (o bloco do Núcleo) a partir de um bloco de estrutura. */
    public static BlockPos centerOf(BlockPos pos, BlockState state) {
        Offset o = Offset.fromIndices(state.getValue(OFFSET_X), state.getValue(OFFSET_Y), state.getValue(OFFSET_Z));
        return pos.offset(-o.dx(), -o.dy(), -o.dz());
    }

    private static int shapeIndex(int x, int y, int z) {
        return x * 9 + y * 3 + z;
    }

    // ---------------------------------------------------------------- formato

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPES[shapeIndex(state.getValue(OFFSET_X), state.getValue(OFFSET_Y), state.getValue(OFFSET_Z))];
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                    @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1.0F;
    }

    /** "Escolher bloco" (botão do meio) dá o item do Núcleo. */
    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos,
                                                @NotNull BlockState state) {
        return new ItemStack(ModItems.QUANTUM_CORE.get());
    }

    // ---------------------------------------------------------------- clique e quebra

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(centerOf(pos, state)) instanceof QuantumCoreBlockEntity core) {
            core.sendStatus(player);
        }
        return InteractionResult.CONSUME;
    }

    /** No criativo, o centro some sem derrubar o item (senão quebrar o Núcleo no criativo criaria um item). */
    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                                                 @NotNull Player player) {
        if (!level.isClientSide && player.isCreative()) {
            BlockPos center = centerOf(pos, state);
            BlockState core = level.getBlockState(center);
            if (core.is(ModBlocks.QUANTUM_CORE.get())) {
                level.setBlock(center, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, center, Block.getId(core)); // som e partículas de quebra
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Este bloco saiu do mundo (quebrado, explodido): quebra o centro, que derruba o item do Núcleo e remove o
     * resto da estrutura. Se o centro já não existe (foi ele que começou), não faz nada.
     */
    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            BlockPos center = centerOf(pos, state);
            if (level.getBlockState(center).is(ModBlocks.QUANTUM_CORE.get())) {
                level.destroyBlock(center, true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
