package org.tinycore.quantum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

/**
 * O "pincel" dos efeitos do Núcleo e da Matriz: caixas de cor sólida, sem textura. Usa o mesmo tipo de desenho do
 * fundo das plaquinhas de nome ({@code RenderType.textBackground()}), o mesmo que o Monitor do Colony Bridge usa:
 * cor por vértice, com transparência (o alfa da cor ARGB).
 *
 * <p>Dois jeitos de pintar:
 * <ul>
 *   <li>{@link #box}/{@link #cube}/{@link #beam}: <b>luz</b> — brilho máximo, igual de dia e de noite (cristal,
 *   raios, luzes);</li>
 *   <li>{@link #solid}: <b>metal</b> — recebe a luz do mundo e escurece as faces de lado e de baixo como um bloco
 *   comum (braços), para não parecer que brilha.</li>
 * </ul>
 *
 * <p>As coordenadas são as da {@code PoseStack} atual (quem chama costuma escalar para pixels de modelo). As faces
 * seguem a ordem anti-horária vista de fora, que é a "frente" para o Minecraft.
 */
public final class EffectBoxes {

    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;
    /** Brilho de cada face no modo metal (como o Minecraft sombreia os blocos): cima, baixo, norte/sul, oeste/leste. */
    private static final float SHADE_UP = 1f;
    private static final float SHADE_DOWN = 0.5f;
    private static final float SHADE_Z = 0.8f;
    private static final float SHADE_X = 0.6f;

    private final VertexConsumer consumer;
    private final PoseStack pose;
    private final int worldLight;
    /** Cantos da seção do {@link #beam}, guardados para não criar arrays a cada barra. */
    private final float[] cornerX = new float[4], cornerY = new float[4], cornerZ = new float[4];

    /**
     * @param worldLight luz do mundo no bloco (o {@code packedLight} que o renderizador recebe), usada só por
     *                   {@link #solid}.
     */
    public EffectBoxes(MultiBufferSource buffers, PoseStack pose, int worldLight) {
        this.consumer = buffers.getBuffer(RenderType.textBackground());
        this.pose = pose;
        this.worldLight = worldLight;
    }

    /** Caixa de luz de {@code (x0, y0, z0)} a {@code (x1, y1, z1)}, cor ARGB ({@code 0xAARRGGBB}). */
    public void box(float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        faces(x0, y0, z0, x1, y1, z1, color, color, color, color, FULL_BRIGHT);
    }

    /** Cubo de luz centrado em {@code (cx, cy, cz)} com meia aresta {@code half}. */
    public void cube(float cx, float cy, float cz, float half, int color) {
        box(cx - half, cy - half, cz - half, cx + half, cy + half, cz + half, color);
    }

    /** Caixa de metal: luz do mundo e faces sombreadas, cor ARGB. Aceita os cantos em qualquer ordem. */
    public void solid(float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        faces(Math.min(x0, x1), Math.min(y0, y1), Math.min(z0, z1), Math.max(x0, x1), Math.max(y0, y1),
                Math.max(z0, z1), shade(color, SHADE_UP), shade(color, SHADE_DOWN), shade(color, SHADE_Z),
                shade(color, SHADE_X), worldLight);
    }

    /**
     * Barra de luz fina de um ponto a outro, de seção quadrada com meia largura {@code half} (raios, filetes
     * inclinados). Os 4 lados bastam: as pontas são finas demais para se ver.
     */
    public void beam(float ax, float ay, float az, float bx, float by, float bz, float half, int color) {
        float dx = bx - ax, dy = by - ay, dz = bz - az;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-4f) {
            return;
        }
        // u e v: dois eixos perpendiculares à barra (produto vetorial com o eixo menos alinhado a ela).
        float ux, uy, uz;
        if (Math.abs(dy) < 0.9f * len) {
            ux = dz; uy = 0; uz = -dx;          // barra × eixo Y
        } else {
            ux = 0; uy = -dz; uz = dy;          // barra × eixo X
        }
        float ul = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux *= half / ul; uy *= half / ul; uz *= half / ul;
        float vx = (dy * uz - dz * uy) / len, vy = (dz * ux - dx * uz) / len, vz = (dx * uy - dy * ux) / len;
        Matrix4f m = pose.last().pose();
        float[] sx = cornerX, sy = cornerY, sz = cornerZ; // reaproveitados: chamado muitas vezes por quadro
        sx[0] = ux + vx; sx[1] = -ux + vx; sx[2] = -ux - vx; sx[3] = ux - vx;
        sy[0] = uy + vy; sy[1] = -uy + vy; sy[2] = -uy - vy; sy[3] = uy - vy;
        sz[0] = uz + vz; sz[1] = -uz + vz; sz[2] = -uz - vz; sz[3] = uz - vz;
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            // cada lado é desenhado nos dois sentidos: a ordem dos cantos não importa e não some de nenhum ângulo
            quad(m, ax + sx[i], ay + sy[i], az + sz[i], bx + sx[i], by + sy[i], bz + sz[i],
                    bx + sx[j], by + sy[j], bz + sz[j], ax + sx[j], ay + sy[j], az + sz[j], color, FULL_BRIGHT);
            quad(m, ax + sx[j], ay + sy[j], az + sz[j], bx + sx[j], by + sy[j], bz + sz[j],
                    bx + sx[i], by + sy[i], bz + sz[i], ax + sx[i], ay + sy[i], az + sz[i], color, FULL_BRIGHT);
        }
    }

    private void faces(float x0, float y0, float z0, float x1, float y1, float z1,
                       int up, int down, int zSide, int xSide, int light) {
        Matrix4f m = pose.last().pose();
        quad(m, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, down, light);   // baixo (-Y)
        quad(m, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, up, light);     // cima (+Y)
        quad(m, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, zSide, light);  // norte (-Z)
        quad(m, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, zSide, light);  // sul (+Z)
        quad(m, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, xSide, light);  // oeste (-X)
        quad(m, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, xSide, light);  // leste (+X)
    }

    private void quad(Matrix4f m, float ax, float ay, float az, float bx, float by, float bz,
                      float cx, float cy, float cz, float dx, float dy, float dz, int color, int light) {
        consumer.addVertex(m, ax, ay, az).setColor(color).setLight(light);
        consumer.addVertex(m, bx, by, bz).setColor(color).setLight(light);
        consumer.addVertex(m, cx, cy, cz).setColor(color).setLight(light);
        consumer.addVertex(m, dx, dy, dz).setColor(color).setLight(light);
    }

    /** Escurece o RGB de uma cor ARGB, mantendo o alfa. */
    private static int shade(int argb, float k) {
        int r = (int) (((argb >> 16) & 0xFF) * k), g = (int) (((argb >> 8) & 0xFF) * k), b = (int) ((argb & 0xFF) * k);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}
