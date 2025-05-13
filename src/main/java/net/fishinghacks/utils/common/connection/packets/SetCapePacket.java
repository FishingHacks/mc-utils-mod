package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

import javax.annotation.Nullable;

public record SetCapePacket(@Nullable String cosmeticName, @Nullable Boolean isMCCapes) implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleSetCape(conn, cosmeticName, isMCCapes != null && isMCCapes);
    }

    @Override
    public PacketType<SetCapePacket> type() {
        return Packets.SET_CAPE;
    }
}
