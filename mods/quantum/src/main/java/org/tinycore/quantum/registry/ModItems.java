package org.tinycore.quantum.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.tinycore.quantum.TcQuantum;
import org.tinycore.quantum.block.core.QuantumCoreItem;
import org.tinycore.quantum.item.EntanglerItem;

/** Itens do mod. O bloco de estrutura do Núcleo não tem item: só existe montado pelo Núcleo. */
public final class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TcQuantum.MOD_ID);

    /** Item do Núcleo: guarda o id da rede quando é quebrado ({@link ModDataComponents#CORE_NETWORK}). */
    public static final DeferredItem<QuantumCoreItem> QUANTUM_CORE = ITEMS.register("quantum_core",
            () -> new QuantumCoreItem(ModBlocks.QUANTUM_CORE.get(),
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<BlockItem> SUPERPOSITION_MATRIX =
            ITEMS.registerSimpleBlockItem("superposition_matrix", ModBlocks.SUPERPOSITION_MATRIX, new Item.Properties());

    /** Entrelaçador: liga blocos a uma rede ({@link ModDataComponents#ENTANGLER_NETWORK}). */
    public static final DeferredItem<EntanglerItem> ENTANGLER = ITEMS.register("entangler",
            () -> new EntanglerItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
