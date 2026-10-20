package org.tinycore.quantum.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.tinycore.quantum.Config;
import org.tinycore.quantum.storage.QuantityFormat;

import java.util.List;

/**
 * <b>TC Módulo de Qubits I–V</b>: encaixa numa Matriz de Superposição (clique com o módulo na Matriz) e aumenta a
 * capacidade dela. O nível define quanto soma (config {@code qubitModuleNCapacity}).
 */
public class QubitModuleItem extends Item {

    private final int tier;

    public QubitModuleItem(int tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public int tier() {
        return tier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        long capacity = Config.qubitModuleCapacities()[tier - 1];
        tooltip.add(Component.translatable("tooltip.tcquantum.qubit_module.capacity",
                QuantityFormat.compact(capacity)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.tcquantum.qubit_module.usage").withStyle(ChatFormatting.DARK_GRAY));
    }
}
