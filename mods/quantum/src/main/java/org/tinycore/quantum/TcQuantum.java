package org.tinycore.quantum;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.tinycore.quantum.network.EntanglementBroadcaster;
import org.tinycore.quantum.network.ModNetwork;
import org.tinycore.quantum.registry.ModBlockEntities;
import org.tinycore.quantum.registry.ModBlocks;
import org.tinycore.quantum.registry.ModCapabilities;
import org.tinycore.quantum.registry.ModCreativeTabs;
import org.tinycore.quantum.registry.ModDataComponents;
import org.tinycore.quantum.registry.ModItems;

/**
 * Entrada do <b>TC Quantum</b>: armazenamento e automação sem cabos. Tudo gira em torno de um Núcleo
 * Quântico; os outros blocos ficam "entrelaçados" com ele. Plano completo em {@code mods/quantum/docs/PLANO.md}.
 *
 * <p>Pacotes (crescem fatia a fatia, ver o {@code CLAUDE.md} do mod):
 * <ul>
 *   <li>{@code storage}: o pool de recursos da rede (mapa recurso → quantidade, com capacidade), a aritmética
 *       saturada dos números gigantes e o texto curto deles — regras puras, testadas sem o jogo;</li>
 *   <li>{@code grid}: as redes do servidor (gravadas no mundo), a energia de cada uma e o que controla o trabalho
 *       por tick (orçamento de tempo, espera com recuo);</li>
 *   <li>{@code block}: a base dos blocos entrelaçados; {@code block.core}: o Núcleo Quântico (cubo 3×3×3);
 *       {@code block.matrix}: a Matriz de Superposição;</li>
 *   <li>{@code item}: o Entrelaçador; {@code network}: pacotes; {@code client}: só no cliente (partículas);</li>
 *   <li>{@code registry}: registros (blocos, itens, componentes, aba do criativo, capabilities).</li>
 * </ul>
 * {@code @Mod} (≈ atributo em C#) diz ao NeoForge que esta classe é a entrada do mod {@value #MOD_ID}. O NeoForge
 * cria a classe uma vez, ao carregar, e passa o "barramento de eventos do mod" ({@code IEventBus}), onde cada
 * registro (blocos, itens, telas) se inscreve.
 */
@Mod(TcQuantum.MOD_ID)
public final class TcQuantum {

    public static final String MOD_ID = "tcquantum";
    public static final Logger LOG = LogUtils.getLogger();

    public TcQuantum(IEventBus modBus, ModContainer container) {
        // COMMON, não SERVER: a config SERVER é sincronizada para os clientes, e nada daqui precisa ir para eles.
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModDataComponents.register(modBus);
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModBlockEntities.register(modBus);
        ModCreativeTabs.register(modBus);
        modBus.addListener(ModCapabilities::register);
        modBus.addListener(ModNetwork::register);
        // Eventos do jogo (não do mod): a cada segundo, linhas de entrelaçamento para quem segura o Entrelaçador.
        NeoForge.EVENT_BUS.addListener(EntanglementBroadcaster::onPlayerTick);
    }
}
