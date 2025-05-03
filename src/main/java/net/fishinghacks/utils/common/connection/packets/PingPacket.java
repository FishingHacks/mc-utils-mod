package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public record PingPacket() implements Packet<PacketHandler> {
    @Override
    public void handle(Connection conn, PacketHandler handler) {
        conn.handlePing();
    }

    @Override
    public PacketType<PingPacket> type() {
        return Packets.PING;
    }
}
