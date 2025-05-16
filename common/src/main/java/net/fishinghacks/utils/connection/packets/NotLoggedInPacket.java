package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.connection.Connection;

public record NotLoggedInPacket() implements Packet<PacketHandler> {
    @Override
    public void handle(Connection conn, PacketHandler handler) {
        Constants.LOG.error("Received a NotLoggedIn packet. This should generally be an error, because the first packet should always be a valid login packet.");
    }

    @Override
    public PacketType<NotLoggedInPacket> type() {
        return Packets.NOT_LOGGED_IN;
    }
}
