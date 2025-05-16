package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

public record InviteNotificationPacket(String slug, String player, boolean trusted) implements Packet<ClientPacketHandler> {
    @Override
    public void handle(Connection conn, ClientPacketHandler handler) {
        handler.handleInvite(slug, player, trusted);
    }

    @Override
    public PacketType<InviteNotificationPacket> type() {
        return Packets.INVITE_NOTIFICATION;
    }
}

