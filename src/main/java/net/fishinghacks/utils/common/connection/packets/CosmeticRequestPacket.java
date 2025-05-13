package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public record CosmeticRequestPacket(CosmeticType cosmeticType, String cosmeticName) implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleGetCosmetic(cosmeticType, cosmeticName, conn);
    }

    @Override
    public PacketType<CosmeticRequestPacket> type() {
        return Packets.COSMETIC_REQUEST;
    }
}
