package org.tinycore.quantum.client.render.matrix;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.tinycore.quantum.block.matrix.SuperpositionMatrixBlock;
import org.tinycore.quantum.block.matrix.SuperpositionMatrixBlockEntity;
import org.tinycore.quantum.client.render.AppearClock;
import org.tinycore.quantum.client.render.EffectBoxes;
import org.tinycore.quantum.client.render.EffectPalette;

/**
 * Desenha as partes animadas da Matriz de Superposição (o JSON tem a base, a espinha e os berços, parados): as
 * células com o conteúdo ({@link MatrixCells}) e as luzes da espinha ({@link MatrixSpine}). Cópia do protótipo
 * {@code tools/prototype/parts/06-*}.
 *
 * <p>Nada aqui decide nada: o estado vem do block entity, sincronizado pelo servidor só quando muda (no máximo
 * 1×/s): rede ligada ({@code visualOnline}), ocupação em % ({@code visualFill}) e os módulos encaixados. Sem energia
 * as células, os pulsos e o anel somem e as luzes apagam; ao ligar, tudo surge em {@link #APPEAR_TICKS} ticks.
 *
 * <p>O modelo olha para o sul; aqui a pose gira como o blockstate ({@code FACING}) e escala para pixels do bloco de
 * baixo, o mesmo sistema do {@code superposition_matrix.json}.
 */
public class MatrixEffectsRenderer implements BlockEntityRenderer<SuperpositionMatrixBlockEntity> {

    private static final int VIEW_DISTANCE = 48;
    private static final float PX = 1f / 16f;
    /** Surgimento: 2,4 s, como no protótipo. */
    private static final int APPEAR_TICKS = 48;

    private final AppearClock clock = new AppearClock(APPEAR_TICKS);

    public MatrixEffectsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull SuperpositionMatrixBlockEntity matrix, float partialTick, @NotNull PoseStack pose,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        Level level = matrix.getLevel();
        if (level == null) {
            return;
        }
        long now = level.getGameTime();
        float seconds = (now % 24_000L + partialTick) / 20f;
        float appear = clock.progress(matrix.getBlockPos(), matrix.visualOnline(), now, partialTick);
        int fill = matrix.visualFill();
        EffectPalette palette = EffectPalette.DEFAULT;
        EffectBoxes boxes = new EffectBoxes(buffers, pose, light);

        pose.pushPose();
        Direction facing = matrix.getBlockState().getValue(SuperpositionMatrixBlock.FACING);
        pose.translate(0.5f, 0f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(-modelRotation(facing)));
        pose.translate(-0.5f, 0f, -0.5f);
        pose.scale(PX, PX, PX);
        MatrixSpine.render(boxes, pose, palette, appear, fill, seconds);
        MatrixCells.render(boxes, pose, palette, matrix.modules(), appear, fill, seconds);
        pose.popPose();
    }

    /** Mesma rotação do blockstate: o modelo olha para o sul e gira de 90 em 90 graus. */
    private static float modelRotation(Direction facing) {
        return switch (facing) {
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    /** As duas metades, com folga para o anel da coroa, que flutua um pouco acima do topo. */
    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull SuperpositionMatrixBlockEntity matrix) {
        BlockPos pos = matrix.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos, pos.above()).inflate(0.25);
    }
}
