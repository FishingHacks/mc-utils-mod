package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public record PingRequestPacket() implements Packet<PacketHandler> {
    @Override
    public void handle(Connection conn, PacketHandler handler) {
        conn.send(new PingPacket());
        // if we receive a pingrequestpacket, we know that the other side is still alive.
        conn.handlePing();
    }

    @Override
    public PacketType<PingRequestPacket> type() {
        return Packets.PING_REQUEST;
    }
}
