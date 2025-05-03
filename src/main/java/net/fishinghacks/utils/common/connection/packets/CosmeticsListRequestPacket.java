package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public record CosmeticsListRequestPacket() implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleListCosmetics(conn);
    }

    @Override
    public PacketType<CosmeticsListRequestPacket> type() {
        return Packets.LIST_REQUEST;
    }
}
