package net.fishinghacks.utils.connection.packets;

import java.util.UUID;

public interface ClientPacketHandler extends PacketHandler {
    void handleInvite(String slug, String invitingPlayer, boolean trusted);
    void handleInviteFailure();
    void handleGetNameResponse(String name);
    void reloadCosmeticForPlayer(UUID player);
}
