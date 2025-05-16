package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

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
