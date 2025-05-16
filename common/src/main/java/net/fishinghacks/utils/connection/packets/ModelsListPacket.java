package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

import java.util.List;

public record ModelsListPacket(List<String> models) implements Packet<PacketHandler> {
    @Override
    public void handle(Connection conn, PacketHandler handler) {}

    @Override
    public PacketType<ModelsListPacket> type() {
        return Packets.LIST_MODELS;
    }
}
