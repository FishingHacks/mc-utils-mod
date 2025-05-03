package net.fishinghacks.utils.common.connection;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.fishinghacks.utils.common.connection.packets.Packet;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private final Gson jsonDecoder = new Gson();

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        JsonElement result = encodePacket(packet);
        if (result.isJsonNull()) {
            try (var writer = new OutputStreamWriter(new ByteBufOutputStream(out))) {
                jsonDecoder.toJson(new EmptyPacket(packet.type().name()), writer);
                writer.write("\r\n");
            }
            return;
        }
        if (!result.isJsonObject())
            throw new IOException("Failed to encode packet, the encoded version is *not* an object");
        result.getAsJsonObject().addProperty("type", packet.type().name());
        try (var writer = new OutputStreamWriter(new ByteBufOutputStream(out))) {
            jsonDecoder.toJson(result, writer);
            writer.write("\r\n");
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Packet<?>> JsonElement encodePacket(T packet) {
        return jsonDecoder.getAdapter((Class<T>) packet.getClass()).toJsonTree(packet);
    }

    private record EmptyPacket(String type) {
    }
}
