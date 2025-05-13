package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public record CosmeticReplyPacket(CosmeticType cosmeticType, String name,
                                  String b64Data) implements Packet<ClientPacketHandler> {
    @Override
    public void handle(Connection conn, ClientPacketHandler handler) {
    }

    @Override
    public PacketType<CosmeticReplyPacket> type() {
        return Packets.COSMETIC_REPLY;
    }
}
