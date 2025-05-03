package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public interface Packet<Handler extends PacketHandler> {
    void handle(Connection conn, Handler handler);
    PacketType<? extends Packet<Handler>> type();
}
