package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

import java.util.UUID;

public record GetCosmeticForPlayer(UUID player) implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleGetCosmeticForPlayer(player, conn);
    }

    @Override
    public PacketType<GetCosmeticForPlayer> type() {
        return Packets.GET_PLAYER_COSMETIC;
    }
}
