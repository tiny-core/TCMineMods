package org.tinycore.quantum.block.matrix;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.block.EntangledBlock;
import org.tinycore.quantum.block.core.QuantumCoreBlockEntity;
import org.tinycore.quantum.grid.NetworkRecord;
import org.tinycore.quantum.item.QubitModuleItem;
import org.tinycore.quantum.registry.ModBlockEntities;

/**
 * <b>TC Matriz de Superposição (Itens)</b>: soma capacidade de itens à rede em que estiver entrelaçada. Não guarda
 * itens: o conteúdo fica no pool da rede (plano, §2.2); a Matriz só diz "a rede pode guardar mais tanto".
 *
 * <p><b>Dois blocos de altura</b>, como o Cloud Link: a metade de baixo ({@link #HALF} = lower) tem o block entity
 * e desenha o modelo inteiro (32 px); a de cima é invisível e só ocupa o espaço e recebe cliques. Quebrar qualquer
 * metade quebra as duas; o item sai só da de baixo (loot table). {@link #FACING} = para onde a frente olha.
 *
 * <p>Colocada perto de um Núcleo, entra sozinha na rede dele ({@code autoEntangleRadius}); senão, pelo Entrelaçador.
 *
 * <p>Módulos de Qubits: clique com o módulo na Matriz encaixa; Shift + clique com a mão vazia tira o último; clique
 * com a mão vazia mostra módulos, capacidade e a ocupação da rede. Quebrar a Matriz derruba os módulos.
 */
public class SuperpositionMatrixBlock extends Block implements EntityBlock, EntangledBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public SuperpositionMatrixBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public BlockPos entanglementPos(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    @Override
    protected @NotNull BlockState rotate(@NotNull BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(@NotNull BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /** Sem sombra de bloco cheio no modelo, que atravessa a metade de cima. */
    @Override
    protected float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1.0F;
    }

    // ---------------------------------------------------------------- block entity

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new SuperpositionMatrixBlockEntity(pos, state) : null;
    }

    /** Só no servidor: uma vez por segundo, manda ao cliente o estado dos efeitos se mudou. */
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                            @NotNull BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.SUPERPOSITION_MATRIX.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> ((SuperpositionMatrixBlockEntity) be).serverTick();
    }

    // ---------------------------------------------------------------- duas metades

    /** Só coloca se o espaço de cima estiver livre; a frente fica virada para o jogador. */
    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /** Coloca a metade de cima e tenta entrar sozinha na rede de um Núcleo próximo. */
    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        if (!(level instanceof ServerLevel server)
                || !(level.getBlockEntity(pos) instanceof SuperpositionMatrixBlockEntity matrix)) {
            return;
        }
        NetworkRecord network = matrix.autoEntangle(server);
        if (network != null && placer instanceof Player player) {
            player.displayClientMessage(Component.translatable("message.tcquantum.entangle.auto",
                    getName(), QuantumCoreBlockEntity.shortId(network.id())), true);
        }
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return true;
        }
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    /** Uma metade sumiu? A outra vira ar (como a porta do vanilla). */
    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction,
                                              @NotNull BlockState neighbor, @NotNull LevelAccessor level,
                                              @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        boolean towardsOther = direction == (half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN);
        if (towardsOther && !(neighbor.is(this) && neighbor.getValue(HALF) != half)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    /** Quebrar a metade de cima no criativo não derruba o item pela de baixo. */
    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                                                 @NotNull Player player) {
        if (!level.isClientSide && state.getValue(HALF) == DoubleBlockHalf.UPPER && player.isCreative()) {
            BlockPos below = pos.below();
            BlockState lower = level.getBlockState(below);
            if (lower.is(this) && lower.getValue(HALF) == DoubleBlockHalf.LOWER) {
                level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, below, Block.getId(lower));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** A metade de baixo saiu do mundo: derruba os módulos e a rede perde a capacidade desta Matriz. */
    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof SuperpositionMatrixBlockEntity matrix) {
            for (int tier : matrix.modules().installed()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                        MatrixInteractions.moduleStack(tier));
            }
            matrix.onRemoved(server);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ---------------------------------------------------------------- módulos e estado

    /** Clique com um Módulo de Qubits: encaixa no primeiro encaixe vazio. Outros itens seguem o clique normal. */
    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,
                                                       @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        if (!(stack.getItem() instanceof QubitModuleItem module)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(entanglementPos(pos, state)) instanceof SuperpositionMatrixBlockEntity matrix)) {
            return ItemInteractionResult.FAIL;
        }
        if (!matrix.insertModule(module.tier())) {
            player.displayClientMessage(Component.translatable("message.tcquantum.matrix.full"), true);
            return ItemInteractionResult.CONSUME;
        }
        stack.consume(1, player);
        MatrixInteractions.moduleInserted(matrix, player);
        return ItemInteractionResult.CONSUME;
    }

    /** Mão vazia: Shift tira o último módulo; sem Shift, mostra o estado da Matriz e da rede. */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(entanglementPos(pos, state)) instanceof SuperpositionMatrixBlockEntity matrix)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            MatrixInteractions.removeModule(matrix, player);
        } else {
            MatrixInteractions.sendStatus(matrix, player);
        }
        return InteractionResult.CONSUME;
    }
}
