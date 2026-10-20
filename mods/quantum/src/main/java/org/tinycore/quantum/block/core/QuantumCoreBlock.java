package org.tinycore.quantum.block.core;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.block.core.CoreFootprint.Offset;
import org.tinycore.quantum.registry.ModBlockEntities;
import org.tinycore.quantum.registry.ModBlocks;

/**
 * <b>TC Núcleo Quântico</b>: o coração da rede. Sem ele carregado a rede não existe (plano, §2).
 *
 * <p>Ocupa um cubo 3×3×3 ({@link CoreFootprint}). Este é o bloco do <b>centro</b>: tem o block entity e desenha o
 * modelo inteiro (o modelo vai de −16 a 32 px, o máximo que o Minecraft aceita). Os outros 26 são
 * {@link CoreStructureBlock}: invisíveis, com colisão, repassam cliques e energia para cá. A técnica é a do
 * Digital Miner do Mekanism: não dá para atravessar o Núcleo, e quebrar qualquer parte quebra o cubo todo.
 *
 * <p>O item é colocado com o bloco clicado como base do cubo ({@link QuantumCoreItem}); só coloca se as 27
 * posições estiverem livres. {@link #ONLINE} é atualizado pelo {@link QuantumCoreBlockEntity} (luz e, depois,
 * a animação).
 *
 * <p>{@code Block implements EntityBlock} = bloco com block entity (mesmo padrão do Colony Bridge).
 */
public class QuantumCoreBlock extends Block implements EntityBlock {

    /** Rede ligada (com energia) ou em espera. */
    public static final BooleanProperty ONLINE = BooleanProperty.create("online");

    /** Contorno do cubo inteiro, visto do centro: a seleção mostra o Núcleo todo. */
    static final VoxelShape CUBE = Block.box(-16, -16, -16, 32, 32, 32);

    public QuantumCoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ONLINE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ONLINE);
    }

    // ---------------------------------------------------------------- formato

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return CUBE;
    }

    /** Colisão só no próprio bloco: cada bloco de estrutura tem a sua. */
    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                    @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.block();
    }

    /** Sem sombra de "bloco cheio" (ambient occlusion) no modelo, que está cercado pelos blocos de estrutura. */
    @Override
    protected float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1.0F;
    }

    // ---------------------------------------------------------------- block entity

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new QuantumCoreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                            @NotNull BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.QUANTUM_CORE.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> ((QuantumCoreBlockEntity) be).serverTick();
    }

    // ---------------------------------------------------------------- colocar e montar

    /** Só coloca se o cubo inteiro couber (blocos substituíveis, dentro do mundo, sem ninguém dentro). */
    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        if (hasRoom(context, context.getClickedPos())) {
            return defaultBlockState();
        }
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide) {
            player.displayClientMessage(Component.translatable("message.tcquantum.core.no_room"), true);
        }
        return null;
    }

    private static boolean hasRoom(BlockPlaceContext context, BlockPos center) {
        Level level = context.getLevel();
        for (Offset o : CoreFootprint.all()) {
            BlockPos pos = center.offset(o.dx(), o.dy(), o.dz());
            if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
                    || !level.getBlockState(pos).canBeReplaced(context)) {
                return false;
            }
        }
        AABB cube = new AABB(center.getX() - 1, center.getY() - 1, center.getZ() - 1,
                center.getX() + 2, center.getY() + 2, center.getZ() + 2);
        return level.getEntitiesOfClass(LivingEntity.class, cube).isEmpty();
    }

    /** Monta os 26 blocos de estrutura e liga o Núcleo à rede (a do item, se ele trazia uma). */
    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        for (Offset o : CoreFootprint.structure()) {
            level.setBlock(pos.offset(o.dx(), o.dy(), o.dz()), CoreStructureBlock.stateFor(o), Block.UPDATE_ALL);
        }
        if (level.getBlockEntity(pos) instanceof QuantumCoreBlockEntity core) {
            core.bindOnPlacement(server, stack);
        }
    }

    // ---------------------------------------------------------------- quebrar

    /**
     * O centro saiu do mundo (quebrado pela estrutura, comando, explosão): solta a rede (ela fica guardada para
     * o item) e remove os blocos de estrutura, que nunca derrubam nada.
     */
    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            if (level.getBlockEntity(pos) instanceof QuantumCoreBlockEntity core) {
                core.onRemoved(server);
            }
            removeStructure(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void removeStructure(Level level, BlockPos center) {
        for (Offset o : CoreFootprint.structure()) {
            BlockPos pos = center.offset(o.dx(), o.dy(), o.dz());
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.CORE_STRUCTURE.get()) && CoreStructureBlock.centerOf(pos, state).equals(center)) {
                level.removeBlock(pos, false);
            }
        }
    }

    // ---------------------------------------------------------------- clique

    /** Até o Terminal existir (fatia 1.5), o clique mostra no chat o estado da rede. */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof QuantumCoreBlockEntity core) {
            core.sendStatus(player);
        }
        return InteractionResult.CONSUME;
    }
}
