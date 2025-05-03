package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public record GetNamePacket() implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleGetName(conn);
    }

    @Override
    public PacketType<GetNamePacket> type() {
        return Packets.GET_NAME;
    }
}
