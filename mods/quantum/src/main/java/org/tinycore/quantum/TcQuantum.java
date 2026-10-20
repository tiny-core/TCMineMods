package org.tinycore.quantum;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Entrada do <b>TC Quantum</b>: armazenamento e automação sem cabos. Tudo gira em torno de um Núcleo
 * Quântico; os outros blocos ficam "entrelaçados" com ele. Plano completo em {@code mods/quantum/docs/PLANO.md}
 * (raiz do workspace).
 *
 * <p>Pacotes (crescem fatia a fatia, ver o {@code CLAUDE.md} do mod):
 * <ul>
 *   <li>{@code storage}: o pool de recursos da rede (mapa recurso → quantidade, com capacidade) e a aritmética
 *       saturada dos números gigantes — regras puras, testadas sem o jogo;</li>
 *   <li>{@code grid}: o que controla o trabalho da rede por tick (orçamento de tempo, espera com recuo).</li>
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
        LOG.debug("TC Quantum carregado");
    }
}
