package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;
import org.jetbrains.annotations.NotNull;

public record CosmeticReplyPacket(CosmeticType cosmeticType, String name,
                                  String b64Data) implements Packet<ClientPacketHandler> {
    @Override
    public void handle(Connection conn, ClientPacketHandler handler) {
    }

    @Override
    public PacketType<CosmeticReplyPacket> type() {
        return Packets.COSMETIC_REPLY;
    }

    @Override
    public @NotNull String toString() {
        return "CosmeticReplyPacket{" + "cosmeticType=" + cosmeticType + ", name='" + name + '\'' + ", b64Data='... " +
            "(" + b64Data.length() + " characters)'" + '}';
    }
}
