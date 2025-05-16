package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

public record DisconnectPacket(String reason) implements Packet<PacketHandler> {
    public void handle(Connection conn, PacketHandler handler) {

    }

    public PacketType<DisconnectPacket> type() {
        return Packets.DISCONNECT;
    }
}
