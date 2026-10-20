package org.tinycore.quantum.client;

import net.minecraft.client.Minecraft;
import org.tinycore.quantum.network.EntanglementLinesPayload;

/**
 * Recebe no cliente os pacotes do servidor. Só é carregada no cliente (ver {@code ModNetwork}).
 */
public final class ClientPayloadHandler {

    private ClientPayloadHandler() {}

    public static void onEntanglementLines(EntanglementLinesPayload payload) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            EntanglementLines.update(payload, level.getGameTime());
        }
    }
}
