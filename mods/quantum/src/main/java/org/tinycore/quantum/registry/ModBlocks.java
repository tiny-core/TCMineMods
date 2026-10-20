package org.tinycore.quantum.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.tinycore.quantum.TcQuantum;
import org.tinycore.quantum.block.core.CoreStructureBlock;
import org.tinycore.quantum.block.core.QuantumCoreBlock;
import org.tinycore.quantum.block.matrix.SuperpositionMatrixBlock;

/**
 * Blocos do mod. {@code DeferredRegister} é uma lista de "coisas a registrar" que o NeoForge preenche no momento
 * certo da inicialização; {@code DeferredBlock} é a referência que só pode ser lida ({@code get()}) depois disso.
 */
public final class ModBlocks {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TcQuantum.MOD_ID);

    /**
     * Bloco principal do Núcleo (centro do cubo 3×3×3). Resistência de obsidiana a explosões: o Núcleo é o coração
     * da rede e não deve sumir com uma creeper. Pistão não move (separaria o cubo).
     */
    public static final DeferredBlock<QuantumCoreBlock> QUANTUM_CORE = BLOCKS.register("quantum_core",
            () -> new QuantumCoreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(5f, 1200f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(QuantumCoreBlock.ONLINE) ? 12 : 4)));

    /** Os 26 blocos invisíveis em volta do centro: colisão, clique e quebra. Nunca derrubam item próprio. */
    public static final DeferredBlock<CoreStructureBlock> CORE_STRUCTURE = BLOCKS.register("core_structure",
            () -> new CoreStructureBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(5f, 1200f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)));

    /** Matriz de Superposição (Itens), 1×2. Pistão não move (separaria as metades). */
    public static final DeferredBlock<SuperpositionMatrixBlock> SUPERPOSITION_MATRIX = BLOCKS.register(
            "superposition_matrix",
            () -> new SuperpositionMatrixBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 12f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> 6)));

    private ModBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
