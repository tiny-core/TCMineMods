package org.tinycore.quantum.block.matrix;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.tinycore.quantum.block.core.QuantumCoreBlockEntity;
import org.tinycore.quantum.grid.MemberKind;
import org.tinycore.quantum.grid.NetworkRecord;
import org.tinycore.quantum.registry.ModItems;
import org.tinycore.quantum.storage.QuantityFormat;
import org.tinycore.quantum.storage.ResourcePool;

/**
 * O que a {@link SuperpositionMatrixBlock} faz ao ser clicada: devolver o módulo tirado ao jogador e as mensagens de
 * estado. Separado do bloco para o bloco cuidar só de colocar, quebrar e repassar os cliques.
 */
final class MatrixInteractions {

    private MatrixInteractions() {}

    static void moduleInserted(SuperpositionMatrixBlockEntity matrix, Player player) {
        player.displayClientMessage(Component.translatable("message.tcquantum.matrix.module_in",
                matrix.modules().count(), MatrixModules.SLOTS,
                QuantityFormat.compact(matrix.memberCapacity())), true);
    }

    static void removeModule(SuperpositionMatrixBlockEntity matrix, Player player) {
        int tier = matrix.removeModule();
        if (tier == 0) {
            player.displayClientMessage(Component.translatable("message.tcquantum.matrix.no_modules"), true);
            return;
        }
        ItemStack module = moduleStack(tier);
        if (!player.getInventory().add(module)) {
            player.drop(module, false);
        }
        NetworkRecord network = matrix.network();
        boolean over = network != null && network.storage().items().overCapacity();
        player.displayClientMessage(Component.translatable(over
                ? "message.tcquantum.matrix.module_out_over" : "message.tcquantum.matrix.module_out",
                QuantityFormat.compact(matrix.memberCapacity())), true);
    }

    static void sendStatus(SuperpositionMatrixBlockEntity matrix, Player player) {
        player.displayClientMessage(Component.translatable("message.tcquantum.matrix.status",
                matrix.modules().count(), MatrixModules.SLOTS, QuantityFormat.compact(matrix.memberCapacity())), false);
        NetworkRecord network = matrix.network();
        if (network == null) {
            player.displayClientMessage(Component.translatable("message.tcquantum.matrix.no_network"), false);
            return;
        }
        ResourcePool<?> items = network.storage().items();
        player.displayClientMessage(Component.translatable("message.tcquantum.matrix.network",
                QuantumCoreBlockEntity.shortId(network.id()), network.members().count(MemberKind.ITEM_MATRIX),
                QuantityFormat.compact(items.stored()), QuantityFormat.compact(items.capacity()), items.types()),
                false);
    }

    static ItemStack moduleStack(int tier) {
        return new ItemStack(ModItems.QUBIT_MODULES.get(tier - 1).get());
    }
}
