package org.tinycore.quantum.block.core;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.registry.ModDataComponents;

import java.util.List;
import java.util.UUID;

/**
 * Item do Núcleo Quântico. Muda onde o bloco é colocado: o bloco clicado vira a <b>base</b> do cubo 3×3×3, e o
 * centro (onde fica o bloco principal) é um bloco acima. Clicando num teto, o cubo cresce para baixo.
 *
 * <p>Também mostra no tooltip a rede que o item carrega (componente {@link ModDataComponents#CORE_NETWORK}).
 */
public class QuantumCoreItem extends BlockItem {

    public QuantumCoreItem(Block block, Properties properties) {
        super(block, properties);
    }

    /**
     * Gancho do {@code BlockItem} para trocar a posição de colocação (o andaime do vanilla usa o mesmo). Devolve
     * {@code null} (não coloca) se o centro não estiver livre.
     */
    @Override
    public @Nullable BlockPlaceContext updatePlacementContext(@NotNull BlockPlaceContext context) {
        BlockPos clicked = context.getClickedPos();
        BlockPos center = context.getClickedFace() == Direction.DOWN ? clicked.below() : clicked.above();
        BlockPlaceContext moved = BlockPlaceContext.at(context, center, context.getClickedFace());
        return moved.getClickedPos().equals(center) ? moved : null;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.tcquantum.core.size").withStyle(ChatFormatting.GRAY));
        UUID network = stack.get(ModDataComponents.CORE_NETWORK.get());
        tooltip.add(network == null
                ? Component.translatable("tooltip.tcquantum.core.new_network").withStyle(ChatFormatting.GRAY)
                : Component.translatable("tooltip.tcquantum.core.network",
                        QuantumCoreBlockEntity.shortId(network)).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
