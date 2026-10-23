package org.tinycore.quantum.client.render.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.tinycore.quantum.client.render.EffectBoxes;
import org.tinycore.quantum.client.render.EffectPalette;

/**
 * Os <b>4 braços flutuantes</b> do Núcleo: 2 longos em C (de −35° a 68°) e 2 curtos no alto (de 40° a 72°), feitos
 * de segmentos blindados de metal com faixas de luz, separados por juntas magnéticas, com garras viradas para o
 * cristal. Raio de {@link #RADIUS} px: podem sair do 3×3×3 (são efeito, não têm colisão).
 *
 * <p>Ligado: giram devagar em volta do cristal, sobem e descem, os segmentos "respiram" (afastam-se e voltam) e as
 * juntas pulsam. Sem energia: descem um pouco, fecham-se e ficam parados e apagados <b>onde estavam</b> (o giro usa
 * o tempo ligado do {@code AppearClock}). No surgimento sobem e acendem.
 *
 * <p>Desenha também a posição de cada junta em {@link #joints} (px, no chão do 3×3×3): é a ponta dos raios
 * ({@link CoreBolts}).
 */
final class CoreArms {

    static final float RADIUS = 20f;
    /** Juntas no total (entre os segmentos de cada braço). */
    static final int MAX_JOINTS = 10;

    /** Ângulo de início e fim (graus) de cada braço; o giro em Y é 90° × índice. */
    private static final float[][] ARMS = {{-35f, 68f}, {40f, 72f}, {-35f, 68f}, {40f, 72f}};
    private static final float SEGMENT_DEGREES = 21f;
    private static final int METAL = 0xFF2A2E38;
    private static final int FRAME = 0xFF3A3F4B;
    private static final float JOINT_HALF = 0.75f;

    /** x, y, z de cada junta neste quadro (preenchido por {@link #render}). */
    final float[] joints = new float[MAX_JOINTS * 3];
    int jointCount;

    /** Desenha os braços; a pose já está no chão do 3×3×3, em pixels. */
    void render(EffectBoxes boxes, PoseStack pose, CoreAnimation a) {
        float t = a.seconds, on = a.off ? 0f : a.arms;
        int strip = EffectPalette.argb(a.light(a.palette.strip()), 1f);
        jointCount = 0;
        for (int k = 0; k < ARMS.length; k++) {
            float a0 = ARMS[k][0], a1 = ARMS[k][1];
            int n = Math.max(2, Math.round((a1 - a0) / SEGMENT_DEGREES));
            float step = (a1 - a0) / n;
            float length = (float) Math.toRadians(step) * RADIUS - 1.6f;
            float bob = -3.5f * (1f - on) + 1.6f * (float) Math.sin(t * 1.2f + k * Math.PI / 2) * on;
            float tilt = -0.1f * (1f - on) + 0.08f * (float) Math.sin(t * 0.8f + k * 1.3f) * on;
            float yaw = a.spin + k * (float) (Math.PI / 2);

            pose.pushPose();
            pose.translate(0f, CoreAnimation.CENTER_Y + bob, 0f);
            pose.mulPose(Axis.YP.rotation(yaw));
            pose.mulPose(Axis.ZP.rotation(tilt));
            for (int i = 0; i < n; i++) {
                float angle = (float) Math.toRadians(a0 + (i + 0.5f) * step);
                float r = RADIUS + gap(i, k, t, on);
                pose.pushPose();
                pose.translate(r * (float) Math.cos(angle), r * (float) Math.sin(angle), 0f);
                pose.mulPose(Axis.ZP.rotation(angle));
                segment(boxes, length, i == n - 1, i == 0 && a0 < 0f, strip);
                pose.popPose();
                if (i < n - 1) {
                    joint(boxes, a, k, i, a0 + (i + 1) * step, t, on, bob, tilt, yaw);
                }
            }
            pose.popPose();
        }
    }

    /** Afastamento de um segmento (respira quando ligado; fecha um pouco quando desligado). */
    private static float gap(float i, int k, float t, float on) {
        return -0.6f * (1f - on) + 0.55f * (float) Math.sin(t * 2.2f - i * 0.8f + k) * on;
    }

    /** Um segmento: eixo x local = para fora do cristal, y = ao longo do braço, z = largura. */
    private static void segment(EffectBoxes boxes, float length, boolean last, boolean firstLow, int strip) {
        float h = length / 2f;
        boxes.solid(-2f, -h, -2.4f, 2f, h, 2.4f, METAL);
        boxes.solid(-0.6f, -h + 0.5f, -2.7f, 2.4f, h - 0.5f, 2.7f, FRAME);
        boxes.box(2.4f, -h + 1f, -0.7f, 2.7f, h - 1f, 0.7f, strip);
        boxes.box(-2.3f, -h + 1.2f, -0.45f, -2f, h - 1.2f, 0.45f, strip);
        if (last) { // garra da ponta de cima
            boxes.solid(-4.4f, h - 1.2f, -1.9f, -1.5f, h + 1.3f, 1.9f, FRAME);
            boxes.box(-5f, h - 0.7f, -1f, -4.4f, h + 0.8f, 1f, strip);
        }
        if (firstLow) { // garra da ponta de baixo (só nos braços longos, que começam abaixo do cristal)
            boxes.solid(-3.8f, -h - 1.1f, -1.7f, -1.3f, -h + 1f, 1.7f, FRAME);
            boxes.box(-4.3f, -h - 0.6f, -0.8f, -3.8f, -h + 0.6f, 0.8f, strip);
        }
    }

    /** Uma junta: cubo de luz que pulsa; guarda a posição dela no mundo do modelo para os raios. */
    private void joint(EffectBoxes boxes, CoreAnimation a, int k, int i, float degrees, float t, float on,
                       float bob, float tilt, float yaw) {
        float angle = (float) Math.toRadians(degrees);
        float r = RADIUS + gap(i + 0.5f, k, t, on);
        float lx = r * (float) Math.cos(angle), ly = r * (float) Math.sin(angle);
        float pulse = Math.max(0f, (float) Math.sin(t * 4f - i * 1.2f + k * 1.7f)) * on;
        int color = a.light(a.palette.strip());
        if (!a.off) {
            color = EffectPalette.mix(color, a.palette.hi(), pulse * 0.8f);
        }
        boxes.cube(lx, ly, 0f, JOINT_HALF * (0.7f + 0.3f * on + 0.35f * pulse), EffectPalette.argb(color, 1f));

        if (jointCount < MAX_JOINTS) { // mesma conta da pose: inclinação em Z, giro em Y, depois sobe até o centro
            float cz = (float) Math.cos(tilt), sz = (float) Math.sin(tilt);
            float x = lx * cz - ly * sz, y = lx * sz + ly * cz;
            float cy = (float) Math.cos(yaw), sy = (float) Math.sin(yaw);
            int o = jointCount++ * 3;
            joints[o] = x * cy;
            joints[o + 1] = y + CoreAnimation.CENTER_Y + bob;
            joints[o + 2] = -x * sy;
        }
    }
}
