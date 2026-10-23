package org.tinycore.quantum.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.tinycore.quantum.TcQuantum;
import org.tinycore.quantum.client.render.core.CoreEffectsRenderer;
import org.tinycore.quantum.client.render.matrix.MatrixEffectsRenderer;
import org.tinycore.quantum.registry.ModBlockEntities;

/**
 * Registros que só existem no cliente. {@code @EventBusSubscriber(value = Dist.CLIENT)} faz o NeoForge carregar esta
 * classe só no cliente, então o servidor dedicado nunca toca nas classes de desenho.
 */
@EventBusSubscriber(modid = TcQuantum.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {}

    /** Liga cada block entity ao renderizador dos efeitos dele. */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.QUANTUM_CORE.get(), CoreEffectsRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SUPERPOSITION_MATRIX.get(), MatrixEffectsRenderer::new);
    }
}
