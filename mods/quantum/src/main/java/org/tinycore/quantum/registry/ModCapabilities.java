package org.tinycore.quantum.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.block.core.CoreStructureBlock;
import org.tinycore.quantum.block.core.QuantumCoreBlockEntity;

/**
 * Capabilities do mod: é por elas que cabos e máquinas de outros mods enxergam os blocos do TC Quantum
 * ("este bloco aceita energia"). No NeoForge 1.21 elas são registradas uma vez, neste evento, por tipo de bloco.
 *
 * <p>O Núcleo aceita energia (FE) por <b>qualquer</b> um dos 27 blocos: o centro responde direto e os blocos de
 * estrutura repassam para o centro. Assim o cabo liga em qualquer face do cubo.
 */
public final class ModCapabilities {

    private ModCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.QUANTUM_CORE.get(),
                (core, side) -> core.energyStorage());
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, blockEntity, side) -> structureEnergy(level, pos, state),
                ModBlocks.CORE_STRUCTURE.get());
    }

    private static @Nullable IEnergyStorage structureEnergy(Level level, BlockPos pos, BlockState state) {
        BlockPos center = CoreStructureBlock.centerOf(pos, state);
        if (!level.isLoaded(center)) {
            return null;
        }
        return level.getBlockEntity(center) instanceof QuantumCoreBlockEntity core ? core.energyStorage() : null;
    }
}
