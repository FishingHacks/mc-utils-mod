package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

public record InviteFailedPacket() implements Packet<ClientPacketHandler> {
    @Override
    public void handle(Connection conn, ClientPacketHandler handler) {
        handler.handleInviteFailure();
    }

    @Override
    public PacketType<InviteFailedPacket> type() {
        return Packets.INVITE_FAILED;
    }
}
