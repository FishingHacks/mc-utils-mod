package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ServerPacketHandler extends PacketHandler {
    void handleInvite(String slug, String invitingPlayer, String invitedPlayer, Connection conn);
    void handleLogin(String name, UUID id, Connection conn);
    void handleGetName(Connection conn);
    void handleListCosmetics(CosmeticType cosmeticType, Connection conn);
    void handleGetCosmetic(CosmeticType cosmeticType, String cosmeticName, Connection conn);
    void handleSetCape(Connection conn, @Nullable String cosmeticName, boolean isMCCapes);
    void handleSetModels(Connection conn, List<String> models);
    void handleAddModel(Connection conn, String model);
    void handleRemoveModel(Connection conn, String model);
    void handleGetCosmeticForPlayer(UUID playerId, Connection conn);
}
