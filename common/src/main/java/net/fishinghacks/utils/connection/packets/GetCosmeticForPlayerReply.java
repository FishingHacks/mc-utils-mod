package net.fishinghacks.utils.connection.packets;

import net.fishinghacks.utils.connection.Connection;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public record GetCosmeticForPlayerReply(UUID player, @Nullable String capeName, boolean isMCCapes, List<String> models) implements Packet<PacketHandler> {
    @Override
    public void handle(Connection conn, PacketHandler handler) {
    }

    @Override
    public PacketType<GetCosmeticForPlayerReply> type() {
        return Packets.GET_PLAYER_COSMETIC_REPLY;
    }
}
