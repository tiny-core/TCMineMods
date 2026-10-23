package org.tinycore.quantum.client.render.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.tinycore.quantum.client.render.EffectBoxes;
import org.tinycore.quantum.client.render.EffectPalette;

/**
 * Os <b>2 anéis orbitais</b> finos à volta do cristal: voxels pequenos em círculo (um em cada quatro falta, como no
 * protótipo), inclinados e a girar em sentidos opostos, na cor {@code win} da paleta. No surgimento abrem-se do
 * centro (crescem de 0 ao raio), um depois do outro.
 */
final class CoreRings {

    /** Raio (px), inclinação em X e em Z (rad), velocidade de giro (rad/s). */
    private static final float[][] RINGS = {
            {10f, (float) (Math.PI / 2.6), 0.25f, 1.1f},
            {12f, (float) (-Math.PI / 3), -0.4f, -0.8f},
    };
    private static final float POINT_STEP = 0.9f;
    private static final float VOXEL_HALF = 0.55f / 2f;

    private CoreRings() {
    }

    /** Desenha os anéis; a pose já está no chão do 3×3×3, em pixels. */
    static void render(EffectBoxes boxes, PoseStack pose, CoreAnimation a) {
        if (a.off) {
            return;
        }
        int color = EffectPalette.argb(a.palette.win(), 1f);
        for (int r = 0; r < RINGS.length; r++) {
            float open = r == 0 ? a.ring1 : a.ring2;
            if (open <= 0f) {
                continue;
            }
            float[] ring = RINGS[r];
            float radius = ring[0] * open;
            int n = Math.max(8, Math.round((float) (2 * Math.PI * ring[0] / POINT_STEP)));
            pose.pushPose();
            pose.translate(0f, CoreAnimation.CENTER_Y, 0f);
            pose.mulPose(Axis.XP.rotation(ring[1]));
            pose.mulPose(Axis.ZP.rotation(ring[2]));
            pose.mulPose(Axis.YP.rotation(a.seconds * ring[3]));
            for (int i = 0; i < n; i++) {
                if (i % 4 == 0) {
                    continue;
                }
                double angle = i * 2 * Math.PI / n;
                boxes.cube((float) Math.cos(angle) * radius, 0f, (float) Math.sin(angle) * radius,
                        VOXEL_HALF * open, color);
            }
            pose.popPose();
        }
    }
}
