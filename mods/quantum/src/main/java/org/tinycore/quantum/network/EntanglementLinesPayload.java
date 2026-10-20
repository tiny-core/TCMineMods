package org.tinycore.quantum.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.tinycore.quantum.TcQuantum;

import java.util.List;
import java.util.Optional;

/**
 * Servidor → cliente: as ligações de uma rede perto do jogador, para desenhar as linhas de partículas enquanto ele
 * segura o Entrelaçador. Enviado a cada segundo ({@link EntanglementBroadcaster}); o cliente apaga as linhas se
 * parar de receber.
 *
 * @param core  posição do Núcleo, se estiver na dimensão do jogador
 * @param links blocos entrelaçados perto do jogador (no máximo {@link #MAX_LINKS})
 */
public record EntanglementLinesPayload(Optional<BlockPos> core, List<Link> links) implements CustomPacketPayload {

    /** Teto de ligações por pacote (pacote pequeno, partículas sob controle). */
    public static final int MAX_LINKS = 128;

    /** Um bloco entrelaçado: posição e cor das partículas (0xRRGGBB). */
    public record Link(BlockPos pos, int color) {
        public static final StreamCodec<ByteBuf, Link> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Link::pos,
                ByteBufCodecs.INT, Link::color,
                Link::new);
    }

    public static final Type<EntanglementLinesPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TcQuantum.MOD_ID, "entanglement_lines"));

    public static final StreamCodec<ByteBuf, EntanglementLinesPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), EntanglementLinesPayload::core,
            Link.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LINKS)), EntanglementLinesPayload::links,
            EntanglementLinesPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
