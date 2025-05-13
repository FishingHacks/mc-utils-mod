package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public record AddModelPacket(String model) implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleAddModel(conn, model);
    }

    @Override
    public PacketType<AddModelPacket> type() {
        return Packets.ADD_MODEL;
    }
}
