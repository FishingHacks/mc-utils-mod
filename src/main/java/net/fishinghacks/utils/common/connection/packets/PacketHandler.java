package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

public interface PacketHandler {
    void onDisconnect(String reason, Connection connection);
}
