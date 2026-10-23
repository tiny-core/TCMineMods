package org.tinycore.quantum.client.render.core;

import org.tinycore.quantum.client.render.EffectBoxes;
import org.tinycore.quantum.client.render.EffectPalette;

import java.util.Random;

/**
 * A <b>coluna de energia</b> que sobe do emissor da base até o cristal: um tubo translúcido que pulsa (cor
 * {@code win}) com um fio claro no meio ({@code hi}) e faíscas a subir por dentro ({@code bolt}). No surgimento a
 * coluna cresce do emissor para cima; as faíscas só aparecem com ela inteira.
 */
final class CoreBeam {

    /** Altura (px, a partir do chão do 3×3×3) do topo do emissor, de onde a coluna sai. */
    private static final float FROM_Y = 12.9f;
    /** A coluna para 6 px abaixo do centro do cristal. */
    private static final float LENGTH = CoreAnimation.CENTER_Y - 6f - FROM_Y;
    private static final float TUBE_HALF = 1.3f;
    private static final float CORE_HALF = 0.35f;
    /** Faíscas: sobem de 14 px até 7 px abaixo do centro do cristal, e voltam a baixo. */
    private static final int SPARKS = 8;
    private static final float SPARK_FROM = 14f;
    private static final float SPARK_SPAN = CoreAnimation.CENTER_Y - 7f - SPARK_FROM;
    private static final float SPARK_SPEED = 9f;
    private static final float SPARK_HALF = 0.35f;
    private static final float[] SPARK_SEEDS = new float[SPARKS * 3];

    static {
        Random r = new Random(77);
        for (int i = 0; i < SPARK_SEEDS.length; i += 3) {
            SPARK_SEEDS[i] = (r.nextFloat() - 0.5f) * 2f;
            SPARK_SEEDS[i + 1] = (r.nextFloat() - 0.5f) * 2f;
            SPARK_SEEDS[i + 2] = r.nextFloat();
        }
    }

    private CoreBeam() {
    }

    /** Desenha a coluna; a pose já está no chão do 3×3×3, em pixels. */
    static void render(EffectBoxes boxes, CoreAnimation a) {
        if (a.off || a.beam <= 0f) {
            return;
        }
        EffectPalette pal = a.palette;
        float top = FROM_Y + LENGTH * a.beam;
        float pulse = (0.3f + 0.25f * (float) Math.sin(a.seconds * 5f)) * (1f + a.flash);
        boxes.box(-CORE_HALF, FROM_Y, -CORE_HALF, CORE_HALF, top, CORE_HALF, EffectPalette.argb(pal.hi(), 1f));
        boxes.box(-TUBE_HALF, FROM_Y, -TUBE_HALF, TUBE_HALF, top, TUBE_HALF, EffectPalette.argb(pal.win(), pulse));
        if (a.beam < 1f) {
            return;
        }
        int spark = EffectPalette.argb(pal.bolt(), 1f);
        for (int i = 0; i < SPARK_SEEDS.length; i += 3) {
            float y = SPARK_FROM + (a.seconds * SPARK_SPEED + SPARK_SEEDS[i + 2] * SPARK_SPAN) % SPARK_SPAN;
            boxes.cube(SPARK_SEEDS[i], y, SPARK_SEEDS[i + 1], SPARK_HALF, spark);
        }
    }
}
