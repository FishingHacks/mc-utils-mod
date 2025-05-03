package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public record GetNameReplyPacket(String name) implements Packet<ClientPacketHandler> {
    @Override
    public void handle(Connection conn, ClientPacketHandler handler) {
        handler.handleGetNameResponse(name);
    }

    @Override
    public PacketType<GetNameReplyPacket> type() {
        return Packets.GET_NAME_REPLY;
    }
}
