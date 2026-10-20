package org.tinycore.quantum.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.tinycore.quantum.client.ClientPayloadHandler;

/**
 * Pacotes do mod. O tratamento no cliente fica dentro de uma lambda: a classe {@link ClientPayloadHandler} (que usa
 * classes do cliente) só é carregada quando um pacote chega, o que nunca acontece no servidor dedicado.
 */
public final class ModNetwork {

    /** Mudar quando o formato de algum pacote mudar (cliente e servidor precisam casar). */
    private static final String PROTOCOL_VERSION = "1";

    private ModNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(EntanglementLinesPayload.TYPE, EntanglementLinesPayload.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.onEntanglementLines(payload));
    }
}
