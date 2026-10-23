package org.tinycore.quantum.client.render.matrix;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.tinycore.quantum.block.matrix.GaugeLevels;
import org.tinycore.quantum.client.render.AppearClock;
import org.tinycore.quantum.client.render.EffectBoxes;
import org.tinycore.quantum.client.render.EffectPalette;

/**
 * As luzes da <b>espinha</b> da Matriz (a coluna de trás, como num rack de servidor): a linha de dados, os 2
 * medidores de 10 segmentos, a luz de estado da coroa, os pulsos de dados que correm entre as células e o anel que
 * flutua por cima da coroa.
 *
 * <p>O JSON tem estas luzes "congeladas" num estado aceso (é o que o exportador viu); aqui são pintadas por cima
 * com caixas {@link #COVER} px maiores, na cor certa do momento — inclusive apagadas. As posições copiam o
 * {@code superposition_matrix.json} (pixels do bloco de baixo, frente a sul): mudou no Blockbench, mudar aqui.
 *
 * <p>Medidores: as regras (segmentos acesos, faixa de cor, piscar) são as de {@link GaugeLevels}, as mesmas do
 * servidor; a cor da faixa normal é a da paleta.
 */
final class MatrixSpine {

    private static final float COVER = 0.03f;
    private static final int AMBER = 0xFBBF24;
    private static final int RED = 0xF87171;
    private static final int RED_DIM = 0x5B1D1D;
    private static final int GREEN = 0x4ADE80;

    /** Linha de dados na frente da espinha. */
    private static final float[] SPINE_LINE = {7.65f, 4.7f, 4.8f, 8.35f, 30.3f, 4.95f};
    /** Medidores: x de cada lado, primeiro segmento em y, passo, altura e profundidade (z). */
    private static final float[][] GAUGE_X = {{5.6f, 5.8f}, {10.2f, 10.4f}};
    private static final int SEGMENTS = 10;
    private static final float GAUGE_Y = 5.2f, GAUGE_STEP = 2.5f, GAUGE_H = 1.8f, GAUGE_Z0 = 1f, GAUGE_Z1 = 3.8f;
    /** Luz de estado na coroa. */
    private static final float[] STATUS = {6.8f, 31.1f, 5.2f, 9.2f, 31.7f, 5.35f};
    /** Pulsos: correm na frente da espinha, de 4,8 a 30,2 px. */
    private static final int PULSES = 3;
    private static final float PULSE_FROM = 4.8f, PULSE_SPAN = 25.4f, PULSE_Z = 4.9f;
    /** Anel da coroa: centro, raio, largura e número de segmentos. */
    private static final float CROWN_X = 8f, CROWN_Y = 32.6f, CROWN_Z = 9f;
    private static final float CROWN_R = 2.2f, CROWN_W = 0.4f;
    private static final int CROWN_SEGMENTS = 14;

    private MatrixSpine() {
    }

    /**
     * @param appear progresso do surgimento (0 = sem energia)
     * @param fill   ocupação da rede em % (0 a 100), já calculada pelo servidor
     */
    static void render(EffectBoxes boxes, PoseStack pose, EffectPalette pal, float appear, int fill, float t) {
        boolean off = appear <= 0f;
        float lights = AppearClock.stage(appear, 0f, 0.2f);
        float spine = AppearClock.stage(appear, 0.05f, 0.35f);

        int line = off ? EffectPalette.DARK : EffectPalette.mix(EffectPalette.DARK, pal.win(), spine);
        cover(boxes, SPINE_LINE, line);
        gauges(boxes, pal, off, spine, fill, t);
        cover(boxes, STATUS, status(off, fill, t));
        if (off) {
            return;
        }
        if (appear >= 1f) {
            int pulse = EffectPalette.argb(pal.hi(), 1f);
            for (int i = 0; i < PULSES; i++) {
                float k = (t * 0.55f + i / (float) PULSES) % 1f;
                float y = PULSE_FROM + PULSE_SPAN * (i % 2 == 0 ? k : 1f - k);
                boxes.box(7.55f, y - 0.45f, PULSE_Z - 0.3f, 8.45f, y + 0.45f, PULSE_Z + 0.3f, pulse);
            }
        }
        if (appear >= 0.9f) {
            crown(boxes, pose, EffectPalette.argb(EffectPalette.mix(EffectPalette.DARK, pal.strip(), lights), 1f), t);
        }
    }

    private static void gauges(EffectBoxes boxes, EffectPalette pal, boolean off, float spine, int fill, float t) {
        int lit = Math.round(GaugeLevels.litSegments(fill, SEGMENTS) * spine);
        int color = switch (GaugeLevels.band(fill)) {
            case FULL -> RED;
            case HIGH -> AMBER;
            default -> pal.strip();
        };
        boolean blinkOff = GaugeLevels.blinksLast(fill) && Math.sin(t * 6f) < 0;
        for (int i = 0; i < SEGMENTS; i++) {
            int c;
            if (off) {
                c = EffectPalette.DARK;
            } else if (i < lit && !(blinkOff && i == lit - 1)) {
                c = color;
            } else {
                c = EffectPalette.UNLIT;
            }
            float y = GAUGE_Y + i * GAUGE_STEP;
            for (float[] x : GAUGE_X) {
                boxes.box(x[0] - COVER, y - COVER, GAUGE_Z0 - COVER, x[1] + COVER, y + GAUGE_H + COVER,
                        GAUGE_Z1 + COVER, EffectPalette.argb(c, 1f));
            }
        }
    }

    /** Luz de estado: verde, âmbar a partir de 80 %, vermelho a piscar a partir de 95 %. */
    private static int status(boolean off, int fill, float t) {
        if (off) {
            return EffectPalette.DARK;
        }
        return switch (GaugeLevels.band(fill)) {
            case FULL -> Math.sin(t * 5f) > 0 ? RED : RED_DIM;
            case HIGH -> AMBER;
            default -> GREEN;
        };
    }

    /** Anel fino de segmentos que gira e flutua por cima da coroa. */
    private static void crown(EffectBoxes boxes, PoseStack pose, int color, float t) {
        float length = (float) (2 * Math.PI * CROWN_R / CROWN_SEGMENTS) + 0.35f;
        pose.pushPose();
        pose.translate(CROWN_X, CROWN_Y + 0.3f * (float) Math.sin(t * 1.6f), CROWN_Z);
        pose.mulPose(Axis.YP.rotation(t * 0.8f));
        for (int i = 0; i < CROWN_SEGMENTS; i++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotation((float) (i * 2 * Math.PI / CROWN_SEGMENTS)));
            boxes.box(CROWN_R - CROWN_W, -0.15f, -length / 2f, CROWN_R, 0.15f, length / 2f, color);
            pose.popPose();
        }
        pose.popPose();
    }

    private static void cover(EffectBoxes boxes, float[] b, int rgb) {
        boxes.box(b[0] - COVER, b[1] - COVER, b[2] - COVER, b[3] + COVER, b[4] + COVER, b[5] + COVER,
                EffectPalette.argb(rgb, 1f));
    }
}
