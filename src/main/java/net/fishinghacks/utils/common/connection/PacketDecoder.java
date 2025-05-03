package net.fishinghacks.utils.common.connection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.fishinghacks.utils.common.connection.packets.Packet;
import net.fishinghacks.utils.common.connection.packets.Packets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    private final Gson jsonDecoder = new Gson();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() == 0) return;
        try(var input = new InputStreamReader(new ByteBufInputStream(in))) {
            JsonObject obj = jsonDecoder.fromJson(input, JsonObject.class);
            String key = obj.remove("type").getAsString();

            if(in.readableBytes() != 0) {
                throw new IOException("Request was not entirely JSON (Packet type: " + key + ")");
            }
            var packetClass = Packets.getPacket(key);
            if(packetClass == null) {
                throw new IOException("No packet for `" + key + "` registered!");
            }
            Packet<?> packet = jsonDecoder.getAdapter(packetClass).fromJsonTree(obj);
            out.add(packet);
        }
    }
}
