package org.tinycore.quantum.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.block.EntangledBlock;
import org.tinycore.quantum.block.EntangledBlockEntity;
import org.tinycore.quantum.block.core.QuantumCoreBlockEntity;
import org.tinycore.quantum.registry.ModDataComponents;

import java.util.List;
import java.util.UUID;

/**
 * <b>TC Entrelaçador</b>: liga blocos a uma rede (plano, §2.1).
 *
 * <ul>
 *   <li>Clique num Núcleo: o item guarda a rede dele (fica brilhando, como um item encantado).</li>
 *   <li>Clique num bloco entrelaçável ({@link EntangledBlock}): o bloco entra na rede guardada.</li>
 *   <li>Shift + clique num bloco: o bloco sai da rede. Shift + usar no ar: o item esquece a rede.</li>
 *   <li>Na mão, mostra as ligações dos blocos até o Núcleo ({@code EntanglementBroadcaster}).</li>
 * </ul>
 *
 * <p>Usa {@code onItemUseFirst} (gancho do NeoForge que roda <b>antes</b> do clique do bloco): sem isso, clicar no
 * Núcleo com o Entrelaçador mostraria o estado da rede em vez de sintonizar o item. Toda decisão é no servidor; o
 * cliente só devolve "usado" para o braço mexer.
 */
public class EntanglerItem extends Item {

    public EntanglerItem(Properties properties) {
        super(properties);
    }

    /** Rede guardada no item, ou {@code null}. */
    public static @Nullable UUID network(ItemStack stack) {
        return stack.get(ModDataComponents.ENTANGLER_NETWORK.get());
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack stack, @NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        QuantumCoreBlockEntity core = QuantumCoreBlockEntity.at(level, pos, state);
        EntangledBlockEntity target = core == null ? entangledAt(level, pos, state) : null;
        if (core == null && target == null) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server) || context.getPlayer() == null) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (core != null) {
            tune(stack, core, player);
        } else if (player.isShiftKeyDown()) {
            target.disentangle(server);
            say(player, Component.translatable("message.tcquantum.entangle.unlinked", blockName(target)));
        } else {
            link(stack, server, target, player);
        }
        return InteractionResult.CONSUME;
    }

    private static @Nullable EntangledBlockEntity entangledAt(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof EntangledBlock block)) {
            return null;
        }
        return level.getBlockEntity(block.entanglementPos(pos, state)) instanceof EntangledBlockEntity be ? be : null;
    }

    private static void tune(ItemStack stack, QuantumCoreBlockEntity core, Player player) {
        UUID id = core.boundNetworkId();
        if (id == null) {
            return;
        }
        stack.set(ModDataComponents.ENTANGLER_NETWORK.get(), id);
        say(player, Component.translatable("message.tcquantum.entangle.tuned", QuantumCoreBlockEntity.shortId(id)));
    }

    private static void link(ItemStack stack, ServerLevel server, EntangledBlockEntity target, Player player) {
        UUID id = network(stack);
        if (id == null) {
            say(player, Component.translatable("message.tcquantum.entangle.not_tuned"));
            return;
        }
        String shortId = QuantumCoreBlockEntity.shortId(id);
        if (id.equals(target.networkId())) {
            say(player, Component.translatable("message.tcquantum.entangle.already", blockName(target), shortId));
        } else if (target.entangle(server, id)) {
            say(player, Component.translatable("message.tcquantum.entangle.linked", blockName(target), shortId));
        } else {
            say(player, Component.translatable("message.tcquantum.entangle.missing"));
        }
    }

    /** Shift + usar no ar: o item esquece a rede. */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player,
                                                          @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || network(stack) == null) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            stack.remove(ModDataComponents.ENTANGLER_NETWORK.get());
            say(player, Component.translatable("message.tcquantum.entangle.cleared"));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** Brilho de item encantado enquanto tem rede guardada. */
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return network(stack) != null;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        UUID id = network(stack);
        tooltip.add(id == null
                ? Component.translatable("tooltip.tcquantum.entangler.untuned").withStyle(ChatFormatting.GRAY)
                : Component.translatable("tooltip.tcquantum.entangler.tuned", QuantumCoreBlockEntity.shortId(id))
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.tcquantum.entangler.usage").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component blockName(EntangledBlockEntity be) {
        return be.getBlockState().getBlock().getName();
    }

    private static void say(Player player, Component message) {
        player.displayClientMessage(message, true);
    }
}
