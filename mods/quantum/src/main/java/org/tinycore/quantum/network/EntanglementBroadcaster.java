package org.tinycore.quantum.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.Config;
import org.tinycore.quantum.grid.BlockLocation;
import org.tinycore.quantum.grid.NetworkMember;
import org.tinycore.quantum.grid.NetworkRecord;
import org.tinycore.quantum.grid.QuantumNetworks;
import org.tinycore.quantum.item.EntanglerItem;
import org.tinycore.quantum.registry.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Manda as linhas de entrelaçamento ({@link EntanglementLinesPayload}) a quem segura um Entrelaçador sintonizado,
 * uma vez por segundo. Só blocos na dimensão do jogador e até {@code entangleLinesRange} blocos dele.
 *
 * <p>É o servidor que decide o que mostrar: o cliente não conhece as redes e nunca pede posições.
 */
public final class EntanglementBroadcaster {

    private static final int INTERVAL_TICKS = 20;

    private EntanglementBroadcaster() {}

    /** Evento de tick do jogador (registrado no barramento do jogo em {@code TcQuantum}). */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % INTERVAL_TICKS != 0) {
            return;
        }
        UUID id = heldNetwork(player);
        if (id == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        NetworkRecord network = QuantumNetworks.get(level).get(id);
        if (network != null) {
            PacketDistributor.sendToPlayer(player, linesFor(network, level, player.blockPosition()));
        }
    }

    private static @Nullable UUID heldNetwork(ServerPlayer player) {
        for (ItemStack stack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (stack.is(ModItems.ENTANGLER.get())) {
                UUID id = EntanglerItem.network(stack);
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }

    private static EntanglementLinesPayload linesFor(NetworkRecord network, ServerLevel level, BlockPos player) {
        String dimension = level.dimension().location().toString();
        double range = Config.ENTANGLE_LINES_RANGE.get();
        double rangeSq = range * range;
        List<EntanglementLinesPayload.Link> links = new ArrayList<>();
        for (NetworkMember member : network.members().all()) {
            if (links.size() >= EntanglementLinesPayload.MAX_LINKS) {
                break;
            }
            if (!member.location().dimension().equals(dimension)) {
                continue;
            }
            BlockPos pos = BlockPos.of(member.location().pos());
            if (pos.distSqr(player) <= rangeSq) {
                links.add(new EntanglementLinesPayload.Link(pos, member.kind().color()));
            }
        }
        BlockLocation core = network.core();
        Optional<BlockPos> corePos = core != null && core.dimension().equals(dimension)
                ? Optional.of(BlockPos.of(core.pos()))
                : Optional.empty();
        return new EntanglementLinesPayload(corePos, links);
    }
}
