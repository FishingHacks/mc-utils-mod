package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.Utils;
import net.fishinghacks.utils.common.connection.Connection;

public record NotLoggedInPacket() implements Packet<PacketHandler> {
    @Override
    public void handle(Connection conn, PacketHandler handler) {
        Utils.getLOGGER().error("Received a NotLoggedIn packet. This should generally be an error, because the first packet should always be a valid login packet.");
    }

    @Override
    public PacketType<NotLoggedInPacket> type() {
        return Packets.NOT_LOGGED_IN;
    }
}
