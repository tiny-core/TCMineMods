package org.tinycore.quantum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.tinycore.quantum.block.core.QuantumCoreBlock;
import org.tinycore.quantum.block.core.QuantumCoreBlockEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Desenha as partes animadas do Núcleo Quântico (o JSON só tem as paradas): a esfera de energia (uma casca de voxels
 * sombreada em três tons de roxo) que pulsa e gira em direções que vão mudando, 3 anéis de metal girando com a
 * inclinação vagueando, 6 qubits
 * orbitando e a coroa (um orbe em cada gaiola do topo dos pilares e a auréola octogonal por cima).
 *
 * <p><b>Energia:</b> em espera nada disso aparece. Quando a energia volta, as peças surgem em sequência em
 * {@link #APPEAR_TICKS} ticks (esfera, anéis, qubits, orbes, auréola) e depois seguem a animação normal. Para saber
 * quando a energia voltou, o renderizador lembra o último estado visto de cada Núcleo ({@link #states}); um Núcleo
 * visto pela primeira vez já ligado (mundo carregando) aparece pronto, sem a animação.
 *
 * <p>{@code BlockEntityRenderer} = desenho feito em código a cada frame, só no cliente. Nada aqui muda o jogo: o
 * estado (ligado ou em espera) vem do próprio bloco ({@link QuantumCoreBlock#ONLINE}). Medidas em pixels de modelo,
 * a partir do centro da esfera (6 px acima da base do bloco central, como no protótipo); anéis e qubits reduzidos
 * por {@link #ORBIT_SCALE} para não encostar nos pilares.
 */
public class CoreEffectsRenderer implements BlockEntityRenderer<QuantumCoreBlockEntity> {

    private static final int VIEW_DISTANCE = 64;
    private static final float PX = 1f / 16f;
    private static final float SPHERE_Y = 6f;
    /**
     * Escala dos anéis e qubits em relação ao protótipo: no modelo do autor os pilares ficam a 16 px do centro, e o
     * protótipo usava órbita de 19,5 px. Com 0,72 o anel maior fica em 12,2 px e os qubits em 14 px.
     */
    private static final float ORBIT_SCALE = 0.72f;

    /** Esfera: casca de voxels de {@link #VOXEL} px, raio {@link #SPHERE_RADIUS} voxels (5 px). */
    private static final float VOXEL = 1.0f;
    private static final int SPHERE_RADIUS = 5;
    private static final int SPHERE_DARK = 0x3B0764;
    private static final int SPHERE_MID = 0x7E22CE;
    private static final int SPHERE_LIGHT = 0xD8B4FE;
    private static final VoxelBall SPHERE = new VoxelBall(SPHERE_RADIUS, VOXEL, SPHERE_DARK, SPHERE_MID, SPHERE_LIGHT);

    /**
     * Coroa: um orbe roxo flutuando dentro da gaiola do topo de cada pilar. Centros das gaiolas no modelo do autor, a
     * partir do centro da esfera: (±15, 20,6, ±15) px.
     */
    private static final VoxelBall ORB = new VoxelBall(2, 0.7f, 0x4C1D95, 0xA855F7, 0xF3E8FF);
    private static final float[][] CAGES = {
            {-15f, 20.6f, -15f}, {15f, 20.6f, -15f}, {-15f, 20.6f, 15f}, {15f, 20.6f, 15f},
    };

    /**
     * Auréola: octógono de barras de luz roxa sobre a coroa, como no protótipo: raio 10 px, cantos cortados de 4 px,
     * barras de 0,8 px de largura e 0,6 px de altura a 28 px do chão do modelo, com um friso escuro fino por cima.
     */
    private static final float HALO_Y = 22f;
    private static final float HALO_RADIUS = 10f;
    private static final float HALO_CORNER = 4f;
    private static final float HALO_WIDTH = 0.8f;
    private static final float HALO_HEIGHT = 0.6f;
    private static final int HALO_COLOR = 0xFFC084FC;
    private static final int HALO_RIM = 0xFF30363F;

    /** Duração da animação de aparecer quando a energia volta (30 ticks = 1,5 s). */
    private static final int APPEAR_TICKS = 30;
    /** Esquece Núcleos que não são desenhados há este tempo (saíram de vista ou foram quebrados). */
    private static final long FORGET_AFTER_TICKS = 600;

    private static final int RING_TRIM = 0xFFF97316;
    private static final int RING_FRAME = 0xFF4B5563;
    private static final int RING_LIGHT = 0xFF22B8E8;
    private static final int QUBIT_A = 0xFFF97316;
    private static final int QUBIT_B = 0xFFE0FBFF;

    /** Quanto (graus) a inclinação de cada anel pode se afastar da original. */
    private static final float RING_WANDER = 35f;

    /** Raio (px), velocidade de giro (rad/s), inclinação no eixo X e no eixo Z (graus) de cada anel. */
    private static final float[][] RINGS = {
            {10f, 0.6f, 0f, 0f},
            {13.5f, -0.45f, 60f, 0f},
            {17f, 0.3f, 0f, -60f},
    };

    /** Último estado de energia visto de cada Núcleo e quando ele ligou (só para a animação de aparecer). */
    private final Map<BlockPos, PowerState> states = new HashMap<>();

    private static final class PowerState {
        boolean online;
        long onlineSince = Long.MIN_VALUE;
        long lastSeen;
    }

    public CoreEffectsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull QuantumCoreBlockEntity core, float partialTick, @NotNull PoseStack pose,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        Level level = core.getLevel();
        if (level == null) {
            return;
        }
        boolean online = core.getBlockState().getValue(QuantumCoreBlock.ONLINE);
        long now = level.getGameTime();
        float appear = appearProgress(core.getBlockPos(), online, now, partialTick);
        if (appear <= 0f) {
            return; // em espera: nada animado
        }
        float seconds = (now % 24_000L + partialTick) / 20f;
        // Semente por Núcleo: dois Núcleos nunca giram igual.
        float seed = (core.getBlockPos().hashCode() & 0xFFFF) / 65535f * (float) (2 * Math.PI);
        EffectBoxes boxes = new EffectBoxes(buffers, pose);

        pose.pushPose();
        pose.translate(0.5f, SPHERE_Y * PX, 0.5f);
        pose.scale(PX, PX, PX);
        sphere(boxes, pose, seconds, seed, stage(appear, 0f, 0.35f));
        orbs(boxes, pose, seconds, stage(appear, 0.55f, 0.85f));
        halo(boxes, pose, seconds, stage(appear, 0.7f, 1f));
        pose.scale(ORBIT_SCALE, ORBIT_SCALE, ORBIT_SCALE);
        float rings = stage(appear, 0.15f, 0.6f);
        for (int i = 0; i < RINGS.length; i++) {
            ring(boxes, pose, RINGS[i], i, seconds, seed, rings);
        }
        qubits(boxes, seconds, stage(appear, 0.4f, 0.8f));
        pose.popPose();
    }

    /**
     * Quanto da animação de aparecer já passou (0 = nada visível, 1 = completo). Em espera devolve 0.
     */
    private float appearProgress(BlockPos pos, boolean online, long now, float partialTick) {
        PowerState state = states.get(pos);
        if (state == null) {
            state = new PowerState();
            state.online = online; // visto pela primeira vez: sem animação
            states.put(pos.immutable(), state);
        } else if (online && !state.online) {
            state.onlineSince = now;
        }
        state.online = online;
        state.lastSeen = now;
        if (now % 200 == 0 && states.size() > 1) {
            states.values().removeIf(s -> now - s.lastSeen > FORGET_AFTER_TICKS);
        }
        if (!online) {
            return 0f;
        }
        if (state.onlineSince == Long.MIN_VALUE) {
            return 1f;
        }
        return Math.min(1f, (now - state.onlineSince + partialTick) / APPEAR_TICKS);
    }

    /** Parte da animação de aparecer que cabe a uma peça: 0 antes de {@code from}, 1 depois de {@code to}, suave. */
    private static float stage(float progress, float from, float to) {
        float t = Math.max(0f, Math.min(1f, (progress - from) / (to - from)));
        return t * t * (3f - 2f * t);
    }

    /**
     * Esfera de voxels: cresce do nada ao aparecer; depois pulsa e gira em direções que vão mudando (rotação
     * "aleatória", mas suave: cada eixo soma senos de frequências que não se repetem juntas).
     */
    private static void sphere(EffectBoxes boxes, PoseStack pose, float t, float seed, float grow) {
        if (grow <= 0f) {
            return;
        }
        pose.pushPose();
        float pulse = (0.95f + 0.06f * (float) Math.sin(t * 2.2f)) * grow;
        pose.scale(pulse, pulse, pulse);
        pose.mulPose(Axis.YP.rotation(t * 0.35f + 1.2f * wobble(t, 0.37f, 0.61f, seed)));
        pose.mulPose(Axis.XP.rotation(0.9f * wobble(t, 0.23f, 0.41f, seed * 2f)));
        pose.mulPose(Axis.ZP.rotation(0.8f * wobble(t, 0.29f, 0.53f, seed * 3f)));
        SPHERE.draw(boxes, 0, 0, 0, 0);
        pose.popPose();
    }

    /** Orbes das gaiolas: acendem crescendo; depois sobem e descem desencontrados. */
    private static void orbs(EffectBoxes boxes, PoseStack pose, float t, float grow) {
        if (grow <= 0f) {
            return;
        }
        for (int i = 0; i < CAGES.length; i++) {
            float bob = 0.6f * (float) Math.sin(t * 1.6f + i * 1.7f);
            pose.pushPose();
            pose.translate(CAGES[i][0], CAGES[i][1] + bob, CAGES[i][2]);
            pose.scale(grow, grow, grow);
            ORB.draw(boxes, 0, 0, 0, 0);
            pose.popPose();
        }
    }

    /** Auréola octogonal: desce do alto até o lugar ao aparecer; depois paira devagar. */
    private static void halo(EffectBoxes boxes, PoseStack pose, float t, float grow) {
        if (grow <= 0f) {
            return;
        }
        float hover = 0.35f * (float) Math.sin(t * 1.1f);
        pose.pushPose();
        pose.translate(0f, HALO_Y + hover + (1f - grow) * 6f, 0f);
        pose.scale(grow, 1f, grow);
        octagon(boxes, pose, HALO_RADIUS, HALO_WIDTH, 0f, HALO_HEIGHT, HALO_COLOR);
        octagon(boxes, pose, HALO_RADIUS + 0.3f, 0.3f, HALO_HEIGHT, HALO_HEIGHT + 0.2f, HALO_RIM);
        pose.popPose();
    }

    /**
     * Anel octogonal de barras (mesma forma do {@code octRing} do protótipo): 4 barras retas nos lados e 4 diagonais
     * cortando os cantos.
     */
    private static void octagon(EffectBoxes boxes, PoseStack pose, float r, float w, float y0, float y1, int color) {
        float c = HALO_CORNER;
        boxes.box(-r + c, y0, -r, r - c, y1, -r + w, color);
        boxes.box(-r + c, y0, r - w, r - c, y1, r, color);
        boxes.box(-r, y0, -r + c, -r + w, y1, r - c, color);
        boxes.box(r - w, y0, -r + c, r, y1, r - c, color);
        float half = (c * 1.42f + w) / 2f;
        int[][] corners = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        for (int[] corner : corners) {
            pose.pushPose();
            pose.translate(corner[0] * (r - c / 2f), 0f, corner[1] * (r - c / 2f));
            pose.mulPose(Axis.YP.rotationDegrees(corner[0] * corner[1] > 0 ? -45f : 45f));
            boxes.box(-half, y0, -w / 2f, half, y1, w / 2f, color);
            pose.popPose();
        }
    }

    /**
     * Anel de segmentos (voxels de 1,6 px); a cada 5 segmentos, uma luz ciano. A inclinação de cada anel vagueia até
     * {@link #RING_WANDER} graus em torno da original e o giro acelera e desacelera, de forma diferente em cada anel.
     * Ao aparecer, abre a partir do centro girando mais rápido.
     */
    private static void ring(EffectBoxes boxes, PoseStack pose, float[] ring, int index, float t, float seed,
                             float grow) {
        if (grow <= 0f) {
            return;
        }
        float radius = ring[0];
        int segments = Math.max(12, Math.round((float) (2 * Math.PI * radius / 2.2)));
        int metal = ring == RINGS[0] ? RING_TRIM : RING_FRAME;
        pose.pushPose();
        float phase = seed + index * 2.1f;
        pose.mulPose(Axis.XP.rotationDegrees(ring[2] + RING_WANDER * wobble(t, 0.11f + index * 0.04f, 0.19f, phase)));
        float tiltZ = ring[3] + RING_WANDER * wobble(t, 0.13f + index * 0.03f, 0.17f, phase * 1.7f);
        pose.mulPose(Axis.ZP.rotationDegrees(tiltZ));
        pose.mulPose(Axis.YP.rotation(t * ring[1] + 1.5f * wobble(t, 0.27f, 0.43f, phase * 2.3f) + (1f - grow) * 3f));
        pose.scale(grow, grow, grow);
        for (int s = 0; s < segments; s++) {
            double angle = 2 * Math.PI * s / segments;
            float x = (float) (Math.cos(angle) * radius);
            float z = (float) (Math.sin(angle) * radius);
            boolean light = s % 5 == 0;
            int color = light ? RING_LIGHT : metal;
            boxes.cube(x, 0, z, light ? 0.9f : 0.8f, color);
        }
        pose.popPose();
    }

    /**
     * Movimento "aleatório" suave entre -1 e 1: dois senos de frequências diferentes, deslocados pela semente.
     * Como as frequências não são múltiplas, a soma não forma um ciclo visível.
     */
    private static float wobble(float t, float freqA, float freqB, float seed) {
        return 0.6f * (float) Math.sin(t * freqA + seed) + 0.4f * (float) Math.sin(t * freqB + seed * 1.37f);
    }

    /** 6 qubits em órbita larga, subindo e descendo desencontrados. Ao aparecer, saem do centro até a órbita. */
    private static void qubits(EffectBoxes boxes, float t, float grow) {
        if (grow <= 0f) {
            return;
        }
        for (int i = 0; i < 6; i++) {
            double angle = t * 1.8 + i * Math.PI / 3;
            float x = (float) (Math.cos(angle) * 19.5 * grow);
            float z = (float) (Math.sin(angle) * 19.5 * grow);
            float y = (float) (Math.sin(angle * 2 + i) * 5 * grow);
            boxes.cube(x, y, z, 0.8f, i % 2 == 0 ? QUBIT_A : QUBIT_B);
        }
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    /** O cubo 3×3×3 inteiro: sem isto, os efeitos sumiriam quando o bloco do centro saísse da tela. */
    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull QuantumCoreBlockEntity core) {
        BlockPos center = core.getBlockPos();
        return AABB.encapsulatingFullBlocks(center.offset(-1, -1, -1), center.offset(1, 1, 1));
    }
}
