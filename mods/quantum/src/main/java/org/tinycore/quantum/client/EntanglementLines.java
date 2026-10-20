package org.tinycore.quantum.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;
import org.tinycore.quantum.TcQuantum;
import org.tinycore.quantum.network.EntanglementLinesPayload;
import org.tinycore.quantum.network.EntanglementLinesPayload.Link;

import java.util.List;
import java.util.Optional;

/**
 * Desenha as linhas de entrelaçamento: pontos de partícula que correm de cada bloco até o Núcleo, na cor do tipo
 * de bloco, enquanto o jogador segura o Entrelaçador (o servidor manda as posições a cada segundo).
 *
 * <p>Para não pesar, cada linha recebe só {@link #POINTS_PER_LINE} pontos a cada {@link #SPAWN_INTERVAL} ticks,
 * que avançam um pouco a cada vez (dá a impressão de fluxo). Sem pacote novo por {@link #STALE_TICKS} ticks (o
 * jogador guardou o item), as linhas somem.
 *
 * <p>{@code @EventBusSubscriber(value = Dist.CLIENT)}: o NeoForge só carrega esta classe no cliente.
 */
@EventBusSubscriber(modid = TcQuantum.MOD_ID, value = Dist.CLIENT)
public final class EntanglementLines {

    private static final int SPAWN_INTERVAL = 4;
    private static final int POINTS_PER_LINE = 4;
    private static final long STALE_TICKS = 30;
    private static final float PARTICLE_SIZE = 0.7F;

    private static Optional<BlockPos> core = Optional.empty();
    private static List<Link> links = List.of();
    private static long receivedAt = Long.MIN_VALUE;

    private EntanglementLines() {}

    static void update(EntanglementLinesPayload payload, long gameTime) {
        core = payload.core();
        links = payload.links();
        receivedAt = gameTime;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || links.isEmpty() || core.isEmpty()) {
            return;
        }
        long time = level.getGameTime();
        if (time - receivedAt > STALE_TICKS) {
            links = List.of();
            return;
        }
        if (time % SPAWN_INTERVAL != 0) {
            return;
        }
        Vec3 target = Vec3.atCenterOf(core.get());
        double phase = (time / SPAWN_INTERVAL % 8) / 8.0 / POINTS_PER_LINE;
        for (Link link : links) {
            spawnLine(level, Vec3.atCenterOf(link.pos()), target, link.color(), phase);
        }
    }

    private static void spawnLine(ClientLevel level, Vec3 from, Vec3 to, int color, double phase) {
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(
                ((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F), PARTICLE_SIZE);
        for (int i = 0; i < POINTS_PER_LINE; i++) {
            double t = phase + (double) i / POINTS_PER_LINE;
            Vec3 point = from.lerp(to, t);
            // true = desenha mesmo longe (o padrão corta partículas a mais de 32 blocos da câmera)
            level.addParticle(dust, true, point.x, point.y, point.z, 0, 0, 0);
        }
    }
}
