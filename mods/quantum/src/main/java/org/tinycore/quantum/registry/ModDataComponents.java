package org.tinycore.quantum.registry;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.tinycore.quantum.TcQuantum;

import java.util.UUID;

/**
 * Componentes de item (dados guardados numa {@code ItemStack} desde o 1.20.5, no lugar do antigo NBT do item).
 * {@code persistent} = vai para o disco; {@code networkSynchronized} = vai para o cliente (tooltip).
 */
public final class ModDataComponents {

    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TcQuantum.MOD_ID);

    /**
     * Rede de um Núcleo quebrado. A loot table copia do block entity para o item, e colocar o item copia de volta:
     * é assim que a rede (energia e, depois, o conteúdo) acompanha o Núcleo.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> CORE_NETWORK =
            COMPONENTS.registerComponentType("core_network", builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    /** Rede em que o Entrelaçador foi sintonizado (clicando num Núcleo). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> ENTANGLER_NETWORK =
            COMPONENTS.registerComponentType("entangler_network", builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    private ModDataComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
