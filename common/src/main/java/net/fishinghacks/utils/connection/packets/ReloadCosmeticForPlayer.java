package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

import java.util.UUID;

public record ReloadCosmeticForPlayer(UUID playerName) implements Packet<ClientPacketHandler> {
    @Override
    public void handle(Connection conn, ClientPacketHandler handler) {
        handler.reloadCosmeticForPlayer(playerName);
    }

    @Override
    public PacketType<ReloadCosmeticForPlayer> type() {
        return Packets.RELOAD_COSMETIC;
    }
}
