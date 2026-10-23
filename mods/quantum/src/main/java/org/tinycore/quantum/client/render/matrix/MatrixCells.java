package org.tinycore.quantum.client.render.matrix;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.tinycore.quantum.block.matrix.MatrixModules;
import org.tinycore.quantum.client.render.AppearClock;
import org.tinycore.quantum.client.render.EffectBoxes;
import org.tinycore.quantum.client.render.EffectPalette;

import java.util.Random;

/**
 * As <b>5 células</b> da Matriz, uma por Módulo de Qubits, a flutuar entre as almofadas de cada berço magnético.
 *
 * <p>Cada célula é um cubo de vidro com arestas de luz ({@code strip}) que gira devagar, uma cópia <b>fantasma</b>
 * (só as arestas, {@code win}) que entra e sai de fase deslocada e girada — o "estado de superposição" — e o
 * <b>conteúdo</b>: cubinhos laranja numa grade 3×3×3 que enche de baixo para cima com a ocupação da rede, com um de
 * cada vez a saltar de lugar. Encaixe vazio = almofadas apagadas e sem célula.
 *
 * <p>Por enquanto só existem módulos de itens; fluidos (colunas a ondular) e químicos (gás) do protótipo entram
 * quando houver módulos desses tipos. Surgimento: as almofadas de cada berço acendem e a célula materializa-se, uma
 * de cada vez, de baixo para cima. As posições copiam o {@code superposition_matrix.json} (bloco de baixo).
 */
final class MatrixCells {

    /** Altura do centro de cada berço (px) e onde fica a célula em X e Z. */
    private static final float[] CELL_Y = {6.6f, 12f, 17.4f, 22.8f, 28.2f};
    private static final float CELL_X = 8f, CELL_Z = 9.2f;
    /** Almofadas de luz de cada berço: x dos dois lados, meia altura, z. */
    private static final float[][] PAD_X = {{4.3f, 4.45f}, {11.55f, 11.7f}};
    private static final float PAD_HALF_H = 0.6f, PAD_Z0 = 6.4f, PAD_Z1 = 10.2f, COVER = 0.03f;

    private static final float HALF = 2.1f;
    private static final float EDGE = 0.16f;
    private static final float GHOST_SCALE = 0.92f;
    private static final int GLASS = EffectPalette.argb(0xC4B5FD, 0.16f);
    /** Conteúdo (itens): grade 3×3×3 com passo de 1,25 px, cubinhos de 0,62 px, 3 tons de laranja. */
    private static final float LATTICE = 1.25f, MINI_HALF = 0.31f;
    private static final int[] ITEM_COLORS = {0xFFC2410C, 0xFFF97316, 0xFFFDBA74};
    private static final int[] SHADE = new int[27];
    private static final float HOP_PERIOD = 1.2f, HOP_TIME = 0.6f;

    static {
        Random r = new Random(515);
        for (int k = 0; k < SHADE.length; k++) {
            SHADE[k] = ITEM_COLORS[r.nextInt(ITEM_COLORS.length)];
        }
    }

    private MatrixCells() {
    }

    /**
     * @param modules encaixes da Matriz (nível 0 = vazio)
     * @param appear  progresso do surgimento (0 = sem energia)
     * @param fill    ocupação da rede em % (0 a 100)
     */
    static void render(EffectBoxes boxes, PoseStack pose, EffectPalette pal, MatrixModules modules, float appear, int fill,
                       float t) {
        float f = fill / 100f;
        for (int i = 0; i < CELL_Y.length; i++) {
            boolean filled = i < MatrixModules.SLOTS && modules.tier(i) != 0;
            float padOn = AppearClock.stage(appear, 0.25f + i * 0.11f, 0.35f + i * 0.11f);
            int pad = !filled ? EffectPalette.UNLIT
                    : appear <= 0f ? EffectPalette.DARK : EffectPalette.mix(EffectPalette.DARK, pal.win(), padOn);
            pads(boxes, CELL_Y[i], EffectPalette.argb(pad, 1f));
            float grow = AppearClock.stage(appear, 0.32f + i * 0.11f, 0.5f + i * 0.11f);
            if (filled && grow > 0f) {
                cell(boxes, pose, pal, i, grow, f, t);
            }
        }
    }

    private static void pads(EffectBoxes boxes, float y, int color) {
        for (float[] x : PAD_X) {
            boxes.box(x[0] - COVER, y - PAD_HALF_H - COVER, PAD_Z0 - COVER, x[1] + COVER, y + PAD_HALF_H + COVER,
                    PAD_Z1 + COVER, color);
        }
    }

    private static void cell(EffectBoxes boxes, PoseStack pose, EffectPalette pal, int i, float grow, float f,
                             float t) {
        pose.pushPose();
        pose.translate(CELL_X, CELL_Y[i] + 0.25f * (float) Math.sin(t * 1.5f + i * 1.7f), CELL_Z);
        pose.scale(grow, grow, grow);

        // fantasma: só as arestas, deslocado e girado, a piscar fora de fase
        if (grow >= 1f) {
            float phase = Math.max(0f, (float) Math.sin(t * 2.3f + i * 1.9f))
                    * Math.max(0f, (float) Math.sin(t * 7.1f + i));
            pose.pushPose();
            pose.translate((float) Math.sin(t * 1.7f + i) * 0.6f, (float) Math.cos(t * 1.3f + i * 2) * 0.35f, 0f);
            pose.mulPose(Axis.XP.rotation(0.2f + 0.3f * (float) Math.sin(t + i)));
            pose.mulPose(Axis.YP.rotation(-t * 0.6f));
            pose.mulPose(Axis.ZP.rotation(0.25f * (float) Math.cos(t * 0.8f)));
            edges(boxes, HALF * GHOST_SCALE, EffectPalette.argb(pal.win(), 0.15f + 0.55f * phase));
            pose.popPose();
        }

        // célula: gira devagar, sentidos alternados
        pose.mulPose(Axis.YP.rotation(t * (0.35f + i * 0.06f) * (i % 2 == 1 ? -1f : 1f)));
        items(boxes, i, f, t);
        edges(boxes, HALF, EffectPalette.argb(pal.strip(), 1f));
        boxes.box(-HALF, -HALF, -HALF, HALF, HALF, HALF, GLASS); // vidro por último: translúcido
        pose.popPose();
    }

    /** As 12 arestas de um cubo de meia aresta {@code k}. */
    private static void edges(EffectBoxes boxes, float k, int color) {
        for (int a = -1; a <= 1; a += 2) {
            for (int b = -1; b <= 1; b += 2) {
                boxes.box(a * k - EDGE, -k, b * k - EDGE, a * k + EDGE, k, b * k + EDGE, color);
                boxes.box(-k, a * k - EDGE, b * k - EDGE, k, a * k + EDGE, b * k + EDGE, color);
                boxes.box(a * k - EDGE, b * k - EDGE, -k, a * k + EDGE, b * k + EDGE, k, color);
            }
        }
    }

    /**
     * Itens: cubinhos na grade, de baixo para cima, tantos quanto a ocupação (mínimo 2). A cada
     * {@link #HOP_PERIOD} s um deles salta em arco para outro lugar da grade (escolhido pelo número do ciclo).
     */
    private static void items(EffectBoxes boxes, int cell, float f, float t) {
        int count = Math.max(2, Math.round(f * 27));
        int cycle = (int) Math.floor(t / HOP_PERIOD);
        float q = Math.min(1f, (t - cycle * HOP_PERIOD) / HOP_TIME);
        int hop = Math.floorMod(cycle * 31 + cell * 7, count);
        int target = Math.floorMod(cycle * 17 + cell * 13 + 5, count);
        for (int k = 0; k < count; k++) {
            float x = lattice(k % 3), z = lattice((k / 3) % 3), y = lattice(k / 9);
            if (k == hop) {
                x += (lattice(target % 3) - x) * q;
                z += (lattice((target / 3) % 3) - z) * q;
                y += (lattice(target / 9) - y) * q + (float) Math.sin(q * Math.PI) * 1.2f;
            }
            float s = MINI_HALF * (0.9f + 0.1f * (float) Math.sin(t * 3f + k));
            boxes.cube(x, y, z, s, SHADE[k]); // sem giro próprio (o protótipo gira cada um): a 0,6 px não se vê
        }
    }

    private static float lattice(int index) {
        return (index - 1) * LATTICE;
    }
}
