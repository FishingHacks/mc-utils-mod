package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

public interface PacketHandler {
    void onDisconnect(String reason, Connection connection);
}
