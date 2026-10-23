package org.tinycore.quantum.client.render.core;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.tinycore.quantum.block.core.QuantumCoreBlock;
import org.tinycore.quantum.block.core.QuantumCoreBlockEntity;
import org.tinycore.quantum.client.render.AppearClock;
import org.tinycore.quantum.client.render.EffectBoxes;

/**
 * Desenha as partes animadas do Núcleo Quântico (o {@code quantum_core.json} só tem a base de contenção, parada):
 * o cristal ({@link CoreCrystal}), os anéis ({@link CoreRings}), a coluna de energia ({@link CoreBeam}), os braços
 * flutuantes ({@link CoreArms}) e os raios ({@link CoreBolts}). Cópia do protótipo {@code tools/prototype/parts/01-*}.
 *
 * <p>{@code BlockEntityRenderer} = desenho feito em código a cada quadro, só no cliente. Nada aqui muda o jogo: o
 * estado (ligado ou em espera) vem do próprio bloco ({@link QuantumCoreBlock#ONLINE}). Sem energia ficam só os
 * braços, parados e apagados; quando a energia volta, tudo surge em {@link #APPEAR_TICKS} ticks
 * ({@link AppearClock}).
 *
 * <p>Esta classe só monta o quadro ({@link CoreAnimation}) e chama as peças, na ordem: opacos primeiro, depois os
 * translúcidos (coluna, raios).
 */
public class CoreEffectsRenderer implements BlockEntityRenderer<QuantumCoreBlockEntity> {

    private static final int VIEW_DISTANCE = 64;
    private static final float PX = 1f / 16f;
    /** Surgimento: 2,6 s, como no protótipo. */
    private static final int APPEAR_TICKS = 52;
    private static final float TICKS_PER_SECOND = 20f;

    private final AppearClock clock = new AppearClock(APPEAR_TICKS);
    private final CoreAnimation frame = new CoreAnimation();
    private final CoreArms arms = new CoreArms();
    private final CoreBolts bolts = new CoreBolts();

    public CoreEffectsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull QuantumCoreBlockEntity core, float partialTick, @NotNull PoseStack pose,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        Level level = core.getLevel();
        if (level == null) {
            return;
        }
        BlockPos pos = core.getBlockPos();
        boolean online = core.getBlockState().getValue(QuantumCoreBlock.ONLINE);
        long now = level.getGameTime();
        float appear = clock.progress(pos, online, now, partialTick);
        float seconds = (now % 24_000L + partialTick) / TICKS_PER_SECOND;
        frame.update(seconds, appear, clock.runTicks() / TICKS_PER_SECOND, pos.hashCode());

        EffectBoxes boxes = new EffectBoxes(buffers, pose, light);
        pose.pushPose();
        // origem no chão do 3×3×3, no meio (o bloco do centro está na camada do meio), em pixels de modelo
        pose.translate(0.5f, -1f, 0.5f);
        pose.scale(PX, PX, PX);
        arms.render(boxes, pose, frame);
        CoreCrystal.render(boxes, pose, frame);
        CoreRings.render(boxes, pose, frame);
        CoreBeam.render(boxes, frame);
        bolts.render(boxes, frame, arms);
        pose.popPose();
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    /** O 3×3×3 e mais um bloco para cada lado (os braços saem dele); sem isto, os efeitos sumiriam fora da tela. */
    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull QuantumCoreBlockEntity core) {
        BlockPos center = core.getBlockPos();
        return AABB.encapsulatingFullBlocks(center.offset(-2, -1, -2), center.offset(2, 1, 2));
    }
}
