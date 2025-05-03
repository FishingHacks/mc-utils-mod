package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ServerPacketHandler extends PacketHandler {
    void handleInvite(String slug, String invitingPlayer, String invitedPlayer, Connection conn);
    void handleLogin(String name, UUID id, Connection conn);
    void handleGetName(Connection conn);
    void handleListCosmetics(Connection conn);
    void handleGetCosmetic(Connection conn, String name);
    void handleSetCosmetic(Connection conn, @Nullable String cosmeticName, boolean isMCCapes);
    void handleGetCosmeticForPlayer(UUID playerId, Connection conn);
}
