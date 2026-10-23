package org.tinycore.quantum.client.render.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.tinycore.quantum.client.render.AppearClock;
import org.tinycore.quantum.client.render.EffectBoxes;
import org.tinycore.quantum.client.render.EffectPalette;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * O <b>cristal</b> do Núcleo: uma casca octaédrica de voxels (achatada em cima e embaixo) com um miolo branco.
 *
 * <p>Cópia do protótipo: os voxels respiram e ondulam em forma e cor (entre {@code mid} e {@code bright} da
 * paleta, com faíscas {@code hi}), e de tempos em tempos a casca estica um "espinho" numa direção ao acaso. No
 * surgimento cada voxel chega voando de fora, claro, e escurece ao encaixar; o clarão final pinta tudo de branco.
 *
 * <p>Os voxels (posição, direção, fase) são calculados uma vez, no carregamento da classe; cada quadro só faz contas
 * e desenha 184 cubos da casca + o miolo, sem criar objetos.
 */
final class CoreCrystal {

    /** Lado da grade de voxels e tamanho de cada voxel (px). */
    private static final float STEP = 1.25f;
    private static final float VOXEL_HALF = STEP * 1.04f / 2f;
    /** Casca: pontos com |x| + 0,78·|y| + |z| entre {@code OUTER − SHELL} e {@code OUTER}. */
    private static final float OUTER = 6.5f;
    private static final float SHELL = 2.2f;
    /** Miolo: casca de esfera de raio 2,6 px, voxels de 1,05 px. */
    private static final float INNER_RADIUS = 2.6f;
    private static final float INNER_HALF = 1.05f / 2f;
    /** Duração de cada espinho (s). */
    private static final float SPIKE_PERIOD = 2.3f;

    private static final float[] POS;      // x, y, z de cada voxel
    private static final float[] NORMAL;   // direção para fora
    private static final float[] FLY;      // de onde vem voando no surgimento
    private static final float[] PHASE;
    private static final float[] DELAY;
    private static final float[] INNER;    // x, y, z do miolo
    private static final int COUNT;

    static {
        Random r = new Random(4242);
        List<float[]> points = new ArrayList<>();
        for (int i = -7; i <= 7; i++) {
            for (int j = -8; j <= 8; j++) {
                for (int k = -7; k <= 7; k++) {
                    float x = i * STEP, y = j * STEP, z = k * STEP;
                    float d = Math.abs(x) + Math.abs(y) * 0.78f + Math.abs(z);
                    if (d <= OUTER && d > OUTER - SHELL) {
                        points.add(new float[]{x, y, z});
                    }
                }
            }
        }
        COUNT = points.size();
        POS = new float[COUNT * 3];
        NORMAL = new float[COUNT * 3];
        FLY = new float[COUNT * 3];
        PHASE = new float[COUNT];
        DELAY = new float[COUNT];
        for (int v = 0; v < COUNT; v++) {
            float[] p = points.get(v);
            float len = (float) Math.sqrt(p[0] * p[0] + p[1] * p[1] + p[2] * p[2]);
            PHASE[v] = r.nextFloat() * (float) (2 * Math.PI);
            DELAY[v] = r.nextFloat() * 0.35f;
            for (int a = 0; a < 3; a++) {
                POS[v * 3 + a] = p[a];
                NORMAL[v * 3 + a] = p[a] / len;
                FLY[v * 3 + a] = p[a] / len * (9 + r.nextFloat() * 8) + (r.nextFloat() - 0.5f) * 6;
            }
        }
        List<float[]> inner = new ArrayList<>();
        for (float x = -INNER_RADIUS; x <= INNER_RADIUS; x++) {
            for (float y = -INNER_RADIUS; y <= INNER_RADIUS; y++) {
                for (float z = -INNER_RADIUS; z <= INNER_RADIUS; z++) {
                    float d = (float) Math.sqrt(x * x + y * y + z * z);
                    if (d <= INNER_RADIUS + 0.3f && d > INNER_RADIUS - 1.2f) {
                        inner.add(new float[]{x, y, z});
                    }
                }
            }
        }
        INNER = new float[inner.size() * 3];
        for (int v = 0; v < inner.size(); v++) {
            System.arraycopy(inner.get(v), 0, INNER, v * 3, 3);
        }
    }

    private CoreCrystal() {
    }

    /** Desenha o cristal; a pose já está no chão do 3×3×3, em pixels. */
    static void render(EffectBoxes boxes, PoseStack pose, CoreAnimation a) {
        if (a.off || a.crystal <= 0f) {
            return;
        }
        float t = a.seconds;
        pose.pushPose();
        pose.translate(0f, CoreAnimation.CENTER_Y, 0f);
        // giro em três eixos, na ordem do three.js (X, depois Y, depois Z)
        pose.mulPose(Axis.XP.rotation(0.22f * (float) Math.sin(t * 0.7f)));
        pose.mulPose(Axis.YP.rotation(t * 0.6f));
        pose.mulPose(Axis.ZP.rotation(0.15f * (float) Math.sin(t * 0.5f + 1f)));

        // espinho: uma direção ao acaso por período, a crescer e a voltar
        float spikeTime = t / SPIKE_PERIOD;
        int spikeIndex = (int) Math.floor(spikeTime);
        float sx = CoreAnimation.noise(a.seed, spikeIndex, 1) - 0.5f;
        float sy = CoreAnimation.noise(a.seed, spikeIndex, 2) - 0.5f;
        float sz = CoreAnimation.noise(a.seed, spikeIndex, 3) - 0.5f;
        float sl = (float) Math.sqrt(sx * sx + sy * sy + sz * sz) + 1e-4f;
        sx /= sl; sy /= sl; sz /= sl;
        float spikeEnv = (float) Math.sin((spikeTime - spikeIndex) * Math.PI) * a.crystal;
        float breathe = 0.93f + 0.09f * (float) Math.sin(t * 2.4f);
        EffectPalette pal = a.palette;

        for (int v = 0; v < COUNT; v++) {
            int i = v * 3;
            float nx = NORMAL[i], ny = NORMAL[i + 1], nz = NORMAL[i + 2], ph = PHASE[v];
            float arrive = AppearClock.stage(a.crystal, DELAY[v], DELAY[v] + 0.65f);
            if (arrive <= 0f) {
                continue;
            }
            float shape = 1f + 0.13f * (float) (Math.sin(t * 2.1f + nx * 2.6f + ph)
                    * Math.cos(t * 1.4f + ny * 3.1f - nz * 2.2f)) * a.crystal;
            float dot = nx * sx + ny * sy + nz * sz;
            if (dot > 0f) {
                float d2 = dot * dot;
                shape += 0.35f * d2 * d2 * d2 * spikeEnv; // dot⁶ sem Math.pow
            }
            float s = breathe * shape, away = 1f - arrive;
            float size = VOXEL_HALF * (1f + 0.15f * (float) Math.sin(t * 3f + ph)) * arrive;
            float x = POS[i] * s + FLY[i] * away, y = POS[i + 1] * s + FLY[i + 1] * away;
            float z = POS[i + 2] * s + FLY[i + 2] * away;

            int color = EffectPalette.mix(pal.mid(), pal.bright(),
                    0.5f + 0.5f * (float) Math.sin(t * 1.7f + ph * 2f + POS[i + 1] * 0.35f));
            if (Math.sin(t * 4.5f + ph * 13f) > 0.9) {
                color = EffectPalette.mix(color, pal.hi(), 0.8f);
            }
            if (away > 0f) {
                color = EffectPalette.mix(color, pal.hi(), away * 0.7f); // chega claro e escurece ao encaixar
            }
            if (a.flash > 0f) {
                color = EffectPalette.mix(color, 0xFFFFFF, a.flash * 0.6f);
            }
            boxes.cube(x, y, z, size, EffectPalette.argb(color, 1f));
        }

        float innerScale = AppearClock.stage(a.crystal, 0.3f, 1f)
                * (1f + 0.25f * (float) Math.sin(t * 3.6f) + a.flash * 0.8f);
        if (innerScale > 0f) {
            for (int i = 0; i < INNER.length; i += 3) {
                boxes.cube(INNER[i] * innerScale, INNER[i + 1] * innerScale, INNER[i + 2] * innerScale,
                        INNER_HALF * innerScale, 0xFFFFFFFF);
            }
        }
        pose.popPose();
    }
}
