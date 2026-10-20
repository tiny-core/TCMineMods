package org.tinycore.quantum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.tinycore.quantum.block.matrix.GaugeLevels;
import org.tinycore.quantum.block.matrix.MatrixModules;
import org.tinycore.quantum.block.matrix.SuperpositionMatrixBlock;
import org.tinycore.quantum.block.matrix.SuperpositionMatrixBlockEntity;

/**
 * Desenha as partes vivas da Matriz de Superposição por cima do modelo JSON:
 * <ul>
 *   <li>partículas flutuando nas câmaras que têm módulo (mais partículas quanto mais cheia a rede);</li>
 *   <li>o LED de cada câmara (aceso com módulo, apagado sem);</li>
 *   <li>os medidores de ocupação: 10 segmentos em cada lateral e 12 na frente, cada um acendendo na cor da faixa
 *       ({@link GaugeLevels}); cheia, o último pisca;</li>
 *   <li>a luz de estado no topo da frente (verde, âmbar, vermelha).</li>
 * </ul>
 * Rede em espera ou sem rede: tudo apagado. Os dados vêm do block entity, que o servidor atualiza 1×/s.
 *
 * <p><b>Posições:</b> as do modelo {@code superposition_matrix.json} do autor (em pixels, frente = lado sul). Cada
 * peça do efeito é uma caixa um pouco maior que a peça do modelo, desenhada por cima dela. Mudou o modelo no
 * Blockbench (câmaras, medidores, LEDs)? As constantes daqui precisam acompanhar.
 */
public class MatrixEffectsRenderer implements BlockEntityRenderer<SuperpositionMatrixBlockEntity> {

    private static final int VIEW_DISTANCE = 48;
    private static final float PX = 1f / 16f;
    /** Quanto a caixa do efeito sobra em volta da peça do modelo, para cobri-la sem piscar (z-fighting). */
    private static final float COVER = 0.04f;

    private static final int OFF = 0xFF3A3F4B;
    private static final int ITEM = 0xFFF97316;
    private static final int AMBER = 0xFFFBBF24;
    private static final int RED = 0xFFEF4444;
    private static final int GREEN = 0xFF22C55E;
    private static final int LED_IDLE = 0xFF7C2D12;
    private static final int PARTICLE_A = 0xFFFDBA74;
    private static final int PARTICLE_B = 0xFFFFEDD5;

    /** Base (y) de cada câmara de vidro e a altura comum; câmaras vão de x 3 a 12,5 e de z 15,4 a 16,3. */
    private static final float[] CHAMBER_Y = {4.7f, 11.2f, 17.7f, 24.2f};
    private static final float CHAMBER_H = 4.1f;
    /** LED ao lado de cada câmara (y de baixo); x 2,3–2,8, z 15,4–15,8, altura 0,9. */
    private static final float[] LED_Y = {6.3f, 12.8f, 19.3f, 25.8f};

    public MatrixEffectsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull SuperpositionMatrixBlockEntity matrix, float partialTick, @NotNull PoseStack pose,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        Level level = matrix.getLevel();
        if (level == null) {
            return;
        }
        float seconds = (level.getGameTime() % 24_000L + partialTick) / 20f;
        boolean online = matrix.visualOnline();
        int fill = matrix.visualFill();
        int[] tiers = matrix.modules().toArray();
        EffectBoxes boxes = new EffectBoxes(buffers, pose);

        pose.pushPose();
        Direction facing = matrix.getBlockState().getValue(SuperpositionMatrixBlock.FACING);
        pose.translate(0.5f, 0f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(-modelRotation(facing)));
        pose.translate(-0.5f, 0f, -0.5f);
        pose.scale(PX, PX, PX);

        for (int slot = 0; slot < MatrixModules.SLOTS; slot++) {
            boolean module = tiers[slot] != 0;
            led(boxes, slot, module ? (online ? ITEM : LED_IDLE) : OFF);
            if (module && online) {
                particles(boxes, slot, fill, seconds);
            }
        }
        sideGauges(boxes, online, fill, seconds);
        frontGauge(boxes, online, fill, seconds);
        statusLight(boxes, online, fill);
        pose.popPose();
    }

    /** Mesma rotação do blockstate: o modelo olha para o sul e gira de 90 em 90 graus. */
    private static float modelRotation(Direction facing) {
        return switch (facing) {
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };
    }

    private static void led(EffectBoxes boxes, int slot, int color) {
        cover(boxes, 2.3f, LED_Y[slot], 15.4f, 2.8f, LED_Y[slot] + 0.9f, 15.8f, color);
    }

    /** Pontos de luz passeando dentro do vidro; de 3 a 8 conforme a ocupação. */
    private static void particles(EffectBoxes boxes, int slot, int fill, float t) {
        int count = 3 + fill * 5 / 100;
        float y0 = CHAMBER_Y[slot] + 0.8f;
        float height = CHAMBER_H - 1.6f;
        for (int k = 0; k < count; k++) {
            float phase = k * 1.7f + slot * 0.9f;
            float x = 3.8f + 7.9f * (0.5f + 0.5f * (float) Math.sin(t * 0.9f + phase * 2.1f));
            float y = y0 + height * (0.5f + 0.5f * (float) Math.sin(t * 1.3f + phase));
            boxes.cube(x, y, 15.85f, 0.35f, k % 2 == 0 ? PARTICLE_A : PARTICLE_B);
        }
    }

    /** 10 segmentos em cada lateral (x 0,4–0,75 e 15,25–15,6; z 6,5–9,5), de y 4 para cima a cada 2,45. */
    private static void sideGauges(EffectBoxes boxes, boolean online, int fill, float t) {
        for (int k = 0; k < 10; k++) {
            int color = segmentColor(online, fill, k, 10, t);
            float y = 4f + k * 2.45f;
            cover(boxes, 0.4f, y, 6.5f, 0.75f, y + 2f, 9.5f, color);
            cover(boxes, 15.25f, y, 6.5f, 15.6f, y + 2f, 9.5f, color);
        }
    }

    /** 12 segmentos na frente, à direita das câmaras (x 13,85–14,65; z 15,8–15,98), de y 4,3 a cada 2,05. */
    private static void frontGauge(EffectBoxes boxes, boolean online, int fill, float t) {
        for (int k = 0; k < 12; k++) {
            float y = 4.3f + k * 2.05f;
            cover(boxes, 13.85f, y, 15.8f, 14.65f, y + 1.6f, 15.98f, segmentColor(online, fill, k, 12, t));
        }
    }

    /** Luz no topo da frente (x 6–10, y 30,4–31,3, z 15,9–16,15). */
    private static void statusLight(EffectBoxes boxes, boolean online, int fill) {
        int color = !online ? OFF : switch (GaugeLevels.band(fill)) {
            case NORMAL -> GREEN;
            case HIGH -> AMBER;
            case FULL -> RED;
        };
        cover(boxes, 6f, 30.4f, 15.9f, 10f, 31.3f, 16.15f, color);
    }

    private static int segmentColor(boolean online, int fill, int index, int segments, float t) {
        if (!online) {
            return OFF;
        }
        int lit = GaugeLevels.litSegments(fill, segments);
        if (index >= lit) {
            return OFF;
        }
        if (index == segments - 1 && GaugeLevels.blinksLast(fill) && (int) (t * 2) % 2 == 0) {
            return OFF;
        }
        return switch (GaugeLevels.band(fill)) {
            case NORMAL -> ITEM;
            case HIGH -> AMBER;
            case FULL -> RED;
        };
    }

    /** Caixa um pouco maior que a peça do modelo, para ficar por cima dela. */
    private static void cover(EffectBoxes boxes, float x0, float y0, float z0, float x1, float y1, float z1,
                              int color) {
        boxes.box(x0 - COVER, y0 - COVER, z0 - COVER, x1 + COVER, y1 + COVER, z1 + COVER, color);
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    /** As duas metades: sem isto, os efeitos de cima sumiriam com a metade de baixo fora da tela. */
    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull SuperpositionMatrixBlockEntity matrix) {
        BlockPos pos = matrix.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos, pos.above());
    }
}
