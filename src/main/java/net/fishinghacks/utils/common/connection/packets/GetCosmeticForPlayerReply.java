package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.connection.Connection;

import javax.annotation.Nullable;
import java.util.UUID;

public record GetCosmeticForPlayerReply(UUID player, @Nullable String cosmeticName, boolean isMCCapes) implements Packet<PacketHandler> {
    public GetCosmeticForPlayerReply(UUID player) {
        this(player, null, false);
    }

    @Override
    public void handle(Connection conn, PacketHandler handler) {
    }

    @Override
    public PacketType<GetCosmeticForPlayerReply> type() {
        return Packets.GET_PLAYER_COSMETIC_REPLY;
    }
}
