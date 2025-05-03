package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

import java.util.UUID;

public record LoginPacket(String name, UUID id) implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleLogin(name, id, conn);
    }

    @Override
    public PacketType<LoginPacket> type() {
        return Packets.LOGIN;
    }
}
