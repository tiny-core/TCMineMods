package org.tinycore.quantum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

/**
 * Desenha caixas de cor sólida com luz máxima (brilham igual de dia e de noite): o "pincel" dos efeitos do Núcleo e
 * da Matriz. Usa o mesmo tipo de desenho do fundo das plaquinhas de nome ({@code RenderType.textBackground()}), o
 * mesmo que o Monitor do Colony Bridge usa: cor por vértice, sem textura, com transparência.
 *
 * <p>As coordenadas são as da {@code PoseStack} atual (quem chama costuma escalar para pixels de modelo). As faces
 * seguem a ordem anti-horária vista de fora, que é a "frente" para o Minecraft.
 */
final class EffectBoxes {

    private static final int LIGHT = LightTexture.FULL_BRIGHT;

    private final VertexConsumer consumer;
    private final PoseStack pose;

    EffectBoxes(MultiBufferSource buffers, PoseStack pose) {
        this.consumer = buffers.getBuffer(RenderType.textBackground());
        this.pose = pose;
    }

    /** Caixa de {@code (x0, y0, z0)} a {@code (x1, y1, z1)}, cor ARGB ({@code 0xAARRGGBB}). */
    void box(float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        Matrix4f m = pose.last().pose();
        // baixo (-Y)
        quad(m, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, color);
        // cima (+Y)
        quad(m, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, color);
        // norte (-Z)
        quad(m, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, color);
        // sul (+Z)
        quad(m, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, color);
        // oeste (-X)
        quad(m, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, color);
        // leste (+X)
        quad(m, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, color);
    }

    /** Cubo centrado em {@code (cx, cy, cz)} com meia aresta {@code half}. */
    void cube(float cx, float cy, float cz, float half, int color) {
        box(cx - half, cy - half, cz - half, cx + half, cy + half, cz + half, color);
    }

    private void quad(Matrix4f m, float ax, float ay, float az, float bx, float by, float bz,
                      float cx, float cy, float cz, float dx, float dy, float dz, int color) {
        consumer.addVertex(m, ax, ay, az).setColor(color).setLight(LIGHT);
        consumer.addVertex(m, bx, by, bz).setColor(color).setLight(LIGHT);
        consumer.addVertex(m, cx, cy, cz).setColor(color).setLight(LIGHT);
        consumer.addVertex(m, dx, dy, dz).setColor(color).setLight(LIGHT);
    }
}
