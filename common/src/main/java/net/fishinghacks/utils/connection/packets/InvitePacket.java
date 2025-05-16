package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

public record InvitePacket(String slug, String player) implements Packet<ServerPacketHandler> {
    @Override
    public void handle(Connection conn, ServerPacketHandler handler) {
        handler.handleInvite(slug, conn.getPlayerName(), player, conn);
    }

    @Override
    public PacketType<InvitePacket> type() {
        return Packets.INVITE;
    }
}
