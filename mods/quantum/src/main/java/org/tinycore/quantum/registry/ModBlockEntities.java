package org.tinycore.quantum.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.tinycore.quantum.TcQuantum;
import org.tinycore.quantum.block.core.QuantumCoreBlockEntity;

/** Tipos de block entity. O {@code build(null)} é o padrão do Minecraft (o parâmetro é de data fixer). */
public final class ModBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TcQuantum.MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuantumCoreBlockEntity>> QUANTUM_CORE =
            BLOCK_ENTITIES.register("quantum_core",
                    () -> BlockEntityType.Builder.of(QuantumCoreBlockEntity::new, ModBlocks.QUANTUM_CORE.get())
                            .build(null));

    private ModBlockEntities() {}

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
