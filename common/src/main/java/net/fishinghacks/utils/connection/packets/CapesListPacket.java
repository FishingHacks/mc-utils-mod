package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

import java.util.List;

public record CapesListPacket(List<String> capes) implements Packet<PacketHandler> {
    @Override
    public void handle(Connection conn, PacketHandler handler) {}

    @Override
    public PacketType<CapesListPacket> type() {
        return Packets.LIST_CAPES;
    }
}
