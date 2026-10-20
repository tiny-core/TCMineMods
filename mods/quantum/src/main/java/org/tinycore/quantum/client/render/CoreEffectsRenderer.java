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

/**
 * Desenha as partes animadas do Núcleo Quântico (o JSON só tem as paradas): a esfera de energia em camadas de roxo
 * que pulsa, 3 anéis de metal girando em eixos diferentes e 6 qubits orbitando. Em espera (sem energia) a esfera
 * apaga, os anéis param e os qubits somem.
 *
 * <p>{@code BlockEntityRenderer} = desenho feito em código a cada frame, só no cliente. Nada aqui muda o jogo: o
 * estado (ligado ou em espera) vem do próprio bloco ({@link QuantumCoreBlock#ONLINE}). Medidas em pixels de modelo,
 * a partir do centro da esfera (6 px acima da base do bloco central, como no protótipo).
 */
public class CoreEffectsRenderer implements BlockEntityRenderer<QuantumCoreBlockEntity> {

    private static final int VIEW_DISTANCE = 64;
    private static final float PX = 1f / 16f;
    private static final float SPHERE_Y = 6f;

    private static final int SPHERE_OUTER = 0xCC7E22CE;
    private static final int SPHERE_MID = 0xDDA855F7;
    private static final int SPHERE_INNER = 0xFFF3E8FF;
    private static final int SPHERE_OFF = 0xFF2A2140;
    private static final int RING_TRIM = 0xFFF97316;
    private static final int RING_FRAME = 0xFF4B5563;
    private static final int RING_LIGHT = 0xFF22B8E8;
    private static final int RING_OFF = 0xFF2B2F38;
    private static final int QUBIT_A = 0xFFF97316;
    private static final int QUBIT_B = 0xFFE0FBFF;

    /** Raio (px), velocidade de giro (rad/s), inclinação no eixo X e no eixo Z (graus) de cada anel. */
    private static final float[][] RINGS = {
            {10f, 0.6f, 0f, 0f},
            {13.5f, -0.45f, 60f, 0f},
            {17f, 0.3f, 0f, -60f},
    };

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
        float seconds = (level.getGameTime() % 24_000L + partialTick) / 20f;
        EffectBoxes boxes = new EffectBoxes(buffers, pose);

        pose.pushPose();
        pose.translate(0.5f, SPHERE_Y * PX, 0.5f);
        pose.scale(PX, PX, PX);
        sphere(boxes, pose, seconds, online);
        for (int i = 0; i < RINGS.length; i++) {
            ring(boxes, pose, RINGS[i], i, seconds, online);
        }
        if (online) {
            qubits(boxes, seconds);
        }
        pose.popPose();
    }

    /** Esfera: dois cubos translúcidos girando em sentidos opostos e um miolo claro, pulsando juntos. */
    private static void sphere(EffectBoxes boxes, PoseStack pose, float t, boolean online) {
        if (!online) {
            boxes.cube(0, 0, 0, 3.6f, SPHERE_OFF);
            return;
        }
        float pulse = 0.95f + 0.06f * (float) Math.sin(t * 2.2f);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotation(t * 0.4f));
        pose.mulPose(Axis.XP.rotationDegrees(35f));
        boxes.cube(0, 0, 0, 4.2f * pulse, SPHERE_OUTER);
        pose.popPose();

        pose.pushPose();
        pose.mulPose(Axis.YP.rotation(-t * 0.6f));
        pose.mulPose(Axis.ZP.rotationDegrees(45f));
        boxes.cube(0, 0, 0, 3.3f * pulse, SPHERE_MID);
        pose.popPose();

        boxes.cube(0, 0, 0, 1.8f * pulse, SPHERE_INNER);
    }

    /** Anel de segmentos (voxels de 1,6 px); a cada 5 segmentos, uma luz ciano. */
    private static void ring(EffectBoxes boxes, PoseStack pose, float[] ring, int index, float t, boolean online) {
        float radius = ring[0];
        int segments = Math.max(12, Math.round((float) (2 * Math.PI * radius / 2.2)));
        int metal = !online ? RING_OFF : index == 0 ? RING_TRIM : RING_FRAME;
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(ring[2]));
        pose.mulPose(Axis.ZP.rotationDegrees(ring[3]));
        // Parado em espera, no ângulo de um instante fixo (não volta ao zero, só para).
        pose.mulPose(Axis.YP.rotation(online ? t * ring[1] : index * 0.7f));
        for (int s = 0; s < segments; s++) {
            double angle = 2 * Math.PI * s / segments;
            float x = (float) (Math.cos(angle) * radius);
            float z = (float) (Math.sin(angle) * radius);
            boolean light = s % 5 == 0;
            int color = light && online ? RING_LIGHT : metal;
            boxes.cube(x, 0, z, light ? 0.9f : 0.8f, color);
        }
        pose.popPose();
    }

    /** 6 qubits em órbita larga, subindo e descendo desencontrados. */
    private static void qubits(EffectBoxes boxes, float t) {
        for (int i = 0; i < 6; i++) {
            double angle = t * 1.8 + i * Math.PI / 3;
            float x = (float) (Math.cos(angle) * 19.5);
            float z = (float) (Math.sin(angle) * 19.5);
            float y = (float) (Math.sin(angle * 2 + i) * 5);
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
