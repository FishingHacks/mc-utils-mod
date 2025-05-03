package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

import java.util.List;

public record CosmeticsListPacket(List<String> cosmetics) implements Packet<PacketHandler> {
    @Override
    public void handle(Connection conn, PacketHandler handler) {}

    @Override
    public PacketType<CosmeticsListPacket> type() {
        return Packets.LIST;
    }
}
