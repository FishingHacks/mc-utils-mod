package net.fishinghacks.utils.server.server;

import net.fishinghacks.utils.common.CommonUtil;
import net.fishinghacks.utils.common.config.Configs;
import net.fishinghacks.utils.common.config.CosmeticMapConfigValue;
import net.fishinghacks.utils.common.connection.Connection;
import net.fishinghacks.utils.common.connection.packets.*;
import net.fishinghacks.utils.server.UtilsServer;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record ServerPacketHandlerImpl(Server server, Path cosmeticsDirectory) implements ServerPacketHandler {
    @Override
    public void handleInvite(String slug, String invitingPlayer, String invitedPlayer, Connection conn) {
        if (slug == null || invitingPlayer == null || invitedPlayer == null || slug.isEmpty() || invitingPlayer.isEmpty() || invitedPlayer.isEmpty() || conn.getPlayerName() == null || conn.getPlayerName()
            .isEmpty()) {
            conn.send(new InviteFailedPacket());
            return;
        }
        Connection receiver = server.forPlayer(invitedPlayer);
        if (receiver == null) {
            conn.send(new InviteFailedPacket());
            return;
        }
        receiver.send(new InviteNotificationPacket(slug, invitingPlayer, false),
            Connection.SendListener.onFailure(InviteFailedPacket::new));
    }

    @Override
    public void handleLogin(String name, UUID id, Connection conn) {
        if (name == null || conn.getPlayerName() != null || name.isEmpty()) return;
        conn.setPlayer(name, id);
        server.registerConnectionToPlayer(name, conn);
    }

    @Override
    public void handleGetName(Connection conn) {
        conn.send(new GetNameReplyPacket(Configs.serverConfig.serverName.get()));
    }

    @Override
    public void handleListCosmetics(Connection conn) {
        try (var cosmetics = Files.list(cosmeticsDirectory)) {
            conn.send(new CosmeticsListPacket(cosmetics.map(p -> p.getFileName().toString()).toList()));
        } catch (IOException e) {
            conn.send(new CosmeticsListPacket(List.of()));
        }
    }

    @Override
    public void handleGetCosmetic(Connection conn, String name) {
        if (CommonUtil.isInvalidFilename(name)) {
            conn.send(new CosmeticReplyPacket(name, ""));
            return;
        }
        try {
            String base64 = Arrays.toString(Base64.encodeBase64(Files.readAllBytes(cosmeticsDirectory.resolve(name))));
            conn.send(new CosmeticReplyPacket(name, base64));
        } catch (IOException e) {
            conn.send(new CosmeticReplyPacket(name, ""));
        }
    }

    @Override
    public void handleSetCosmetic(Connection conn, @Nullable String capeName, boolean isMCCapes) {
        if (conn.getPlayerId() == null) return;
        server.schedule(() -> {
            Configs.serverConfig.cosmeticMap.getValue().compute(conn.getPlayerId(), (ignored, cosmetics) -> {
                if (cosmetics == null) cosmetics = new CosmeticMapConfigValue.PlayerCosmetics();
                return cosmetics.updateCape(capeName, isMCCapes);
            });
            Configs.serverConfig.cosmeticMap.updated();
            assert UtilsServer.getServer() != null;
            UtilsServer.getServer().broadcast(new ReloadCosmeticForPlayer(conn.getPlayerId()));
        });
    }

    @Override
    public void handleGetCosmeticForPlayer(UUID playerId, Connection conn) {
        var value = Configs.serverConfig.cosmeticMap.getValue().get(playerId);
        if (value != null) conn.send(new GetCosmeticForPlayerReply(playerId, value.cape, value.capeIsMCCapes));
        else conn.send(new GetCosmeticForPlayerReply(playerId));
    }

    @Override
    public void onDisconnect(String reason, Connection connection) {
    }
}
