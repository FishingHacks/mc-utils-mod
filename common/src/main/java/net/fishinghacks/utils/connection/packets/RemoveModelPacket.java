package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

public record RemoveModelPacket(String model) implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleRemoveModel(conn, model);
    }

    @Override
    public PacketType<RemoveModelPacket> type() {
        return Packets.REMOVE_MODEL;
    }
}
