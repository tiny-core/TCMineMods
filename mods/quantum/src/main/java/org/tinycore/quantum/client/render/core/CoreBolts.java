package org.tinycore.quantum.client.render.core;

import org.tinycore.quantum.client.render.EffectBoxes;
import org.tinycore.quantum.client.render.EffectPalette;

/**
 * Os <b>raios</b> do Núcleo: linhas em ziguezague que saltam da superfície do cristal para o ar ou até uma junta dos
 * braços, e somem em menos de um terço de segundo. Cada raio tem duas linhas (uma branca e uma na cor {@code bolt}),
 * como no protótipo.
 *
 * <p>Sem estado: o tempo é dividido em ciclos por raio, e a forma de cada raio sai de {@link CoreAnimation#noise}
 * com o número do ciclo. Assim o mesmo instante desenha sempre o mesmo raio, e não há nada a guardar entre quadros.
 * No fim do surgimento vem uma rajada: todos os raios acesos ao mesmo tempo.
 */
final class CoreBolts {

    private static final int BOLTS = 8;
    /** Raios por segundo no ritmo normal (o protótipo usa 3,5). */
    private static final float RATE = 3.5f;
    private static final float PERIOD = BOLTS / RATE;
    private static final int POINTS = 9;
    private static final float HALF_WIDTH = 0.12f;
    /** Chance de o raio ir até uma junta dos braços (senão vai para o ar). */
    private static final float TO_JOINT = 0.45f;

    /** Pontos do raio atual (reaproveitados). */
    private final float[] px = new float[POINTS], py = new float[POINTS], pz = new float[POINTS];

    /** Desenha os raios; a pose já está no chão do 3×3×3, em pixels. */
    void render(EffectBoxes boxes, CoreAnimation a, CoreArms arms) {
        if (a.off || a.bolts <= 0f) {
            return;
        }
        for (int b = 0; b < BOLTS; b++) {
            float time = a.seconds + b * PERIOD / BOLTS + CoreAnimation.noise(a.seed, b, 0) * PERIOD;
            int cycle = (int) Math.floor(time / PERIOD);
            float local = time - cycle * PERIOD;
            float life = 0.14f + 0.16f * CoreAnimation.noise(a.seed + b, cycle, 9);
            float alpha = local < life ? 1f - local / life : 0f;
            alpha = Math.max(alpha, a.burst);
            if (alpha <= 0f) {
                continue;
            }
            int key = cycle * BOLTS + b;
            for (int strand = 0; strand < 2; strand++) {
                shape(a, arms, key, strand);
                int color = strand == 0 ? EffectPalette.argb(0xFFFFFF, alpha)
                        : EffectPalette.argb(a.palette.bolt(), alpha * 0.8f);
                for (int i = 0; i + 1 < POINTS; i++) {
                    boxes.beam(px[i], py[i], pz[i], px[i + 1], py[i + 1], pz[i + 1], HALF_WIDTH, color);
                }
            }
        }
    }

    /** Calcula os pontos de uma linha do raio {@code key} em {@link #px}/{@link #py}/{@link #pz}. */
    private void shape(CoreAnimation a, CoreArms arms, int key, int strand) {
        int s = a.seed;
        // direção ao acaso a partir do centro do cristal
        float dx = CoreAnimation.noise(s, key, 1) - 0.5f;
        float dy = CoreAnimation.noise(s, key, 2) - 0.5f;
        float dz = CoreAnimation.noise(s, key, 3) - 0.5f;
        float dl = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) + 1e-4f;
        dx /= dl; dy /= dl; dz /= dl;
        float cy = CoreAnimation.CENTER_Y;
        float ax = dx * 6f, ay = cy + dy * 6f, az = dz * 6f;
        float bx, by, bz;
        if (arms.jointCount > 0 && CoreAnimation.noise(s, key, 4) < TO_JOINT) {
            int j = Math.min(arms.jointCount - 1, (int) (CoreAnimation.noise(s, key, 5) * arms.jointCount)) * 3;
            bx = arms.joints[j]; by = arms.joints[j + 1]; bz = arms.joints[j + 2];
        } else {
            float reach = 13f + 5f * CoreAnimation.noise(s, key, 6);
            bx = dx * reach; by = cy + dy * reach; bz = dz * reach;
        }
        // perp e side: dois eixos perpendiculares à linha, para o ziguezague
        float tx = bx - ax, ty = by - ay, tz = bz - az;
        float rx = CoreAnimation.noise(s, key, 7) - 0.5f, ry = CoreAnimation.noise(s, key, 8) - 0.5f, rz = 0.3f;
        float qx = ry * tz - rz * ty, qy = rz * tx - rx * tz, qz = rx * ty - ry * tx;
        float ql = (float) Math.sqrt(qx * qx + qy * qy + qz * qz) + 1e-4f;
        qx /= ql; qy /= ql; qz /= ql;
        float wx = qy * tz - qz * ty, wy = qz * tx - qx * tz, wz = qx * ty - qy * tx;
        float wl = (float) Math.sqrt(wx * wx + wy * wy + wz * wz) + 1e-4f;
        wx /= wl; wy /= wl; wz /= wl;
        for (int i = 0; i < POINTS; i++) {
            float f = i / (float) (POINTS - 1);
            float env = (float) Math.sin(Math.PI * f) * (2f + strand * 0.4f);
            float jp = (CoreAnimation.noise(key, i, 10 + strand) - 0.5f) * 2f * env;
            float js = (CoreAnimation.noise(key, i, 20 + strand) - 0.5f) * env;
            px[i] = ax + tx * f + qx * jp + wx * js;
            py[i] = ay + ty * f + qy * jp + wy * js;
            pz[i] = az + tz * f + qz * jp + wz * js;
        }
    }
}
