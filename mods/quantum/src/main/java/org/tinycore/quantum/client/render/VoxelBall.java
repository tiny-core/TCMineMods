package org.tinycore.quantum.client.render;

import java.util.ArrayList;
import java.util.List;

/**
 * Bola de voxels: uma casca de cubinhos com cara de esfera, sombreada em três tons (escuro embaixo e atrás, claro em
 * cima e na frente). As posições e cores são calculadas uma vez, na criação; desenhar só percorre os arrays.
 * Usada pela esfera de energia e pelos orbes da coroa do Núcleo.
 */
final class VoxelBall {

    private final float[] positions;
    private final int[] colors;
    private final float half;

    /**
     * @param radius raio em voxels
     * @param voxel  tamanho de cada voxel, em px de modelo
     * @param dark   cor RGB do lado na sombra
     * @param mid    cor RGB do meio
     * @param light  cor RGB do lado iluminado
     */
    VoxelBall(int radius, float voxel, int dark, int mid, int light) {
        List<int[]> voxels = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double d = Math.sqrt(x * x + y * y + z * z);
                    if (d > radius - 1.0 && d <= radius + 0.5) {
                        voxels.add(new int[]{x, y, z});
                    }
                }
            }
        }
        positions = new float[voxels.size() * 3];
        colors = new int[voxels.size()];
        half = voxel / 2f;
        for (int i = 0; i < voxels.size(); i++) {
            int[] v = voxels.get(i);
            positions[i * 3] = v[0] * voxel;
            positions[i * 3 + 1] = v[1] * voxel;
            positions[i * 3 + 2] = v[2] * voxel;
            // Luz vinda de cima e da frente.
            float shade = (0.55f * v[1] + 0.25f * v[0] + 0.35f * v[2]) / radius;
            colors[i] = shade < 0 ? lerp(dark, mid, shade + 1f) : lerp(mid, light, Math.min(1f, shade));
        }
    }

    /**
     * Desenha a bola centrada em {@code (cx, cy, cz)}.
     *
     * @param flatColor 0 = cores sombreadas; outra cor ARGB = a bola inteira nessa cor (apagada, por exemplo)
     */
    void draw(EffectBoxes boxes, float cx, float cy, float cz, int flatColor) {
        for (int i = 0; i < colors.length; i++) {
            boxes.cube(cx + positions[i * 3], cy + positions[i * 3 + 1], cz + positions[i * 3 + 2], half,
                    flatColor != 0 ? flatColor : colors[i]);
        }
    }

    /** Mistura duas cores RGB (t de 0 a 1) e devolve opaca (ARGB). */
    private static int lerp(int from, int to, float t) {
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
