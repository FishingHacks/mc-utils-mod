package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

import java.util.List;

public record SetModelsPacket(List<String> models) implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleSetModels(conn, models);
    }

    @Override
    public PacketType<SetModelsPacket> type() {
        return Packets.SET_MODELS;
    }
}
