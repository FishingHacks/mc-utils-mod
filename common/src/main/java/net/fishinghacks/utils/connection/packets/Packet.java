package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

public interface Packet<Handler extends PacketHandler> {
    void handle(Connection conn, Handler handler);
    PacketType<? extends Packet<Handler>> type();
}
