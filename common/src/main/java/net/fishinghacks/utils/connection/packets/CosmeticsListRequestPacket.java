package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

public record CosmeticsListRequestPacket(CosmeticType cosmeticType) implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleListCosmetics(cosmeticType, conn);
    }

    @Override
    public PacketType<CosmeticsListRequestPacket> type() {
        return Packets.LIST_REQUEST;
    }
}
