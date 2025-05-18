package net.fishinghacks.utils.server;

import net.fishinghacks.utils.CommonUtil;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.config.values.CosmeticMapConfigValue;
import net.fishinghacks.utils.connection.Connection;
import net.fishinghacks.utils.connection.packets.*;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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
    public void handleListCosmetics(CosmeticType type, Connection conn) {
        Function<List<String>, ? extends Packet<PacketHandler>> packetCreator = switch (type) {
            case Cape -> CapesListPacket::new;
            case ModelPreview, ModelData, ModelTexture -> ModelsListPacket::new;
        };
        String extension = type.extension();
        try (var cosmetics = Files.list(cosmeticsDirectory.resolve(type.subdirectory()))) {
            conn.send(packetCreator.apply(cosmetics.map(Path::getFileName).map(Path::toString)
                .filter(v -> v.length() > extension.length() && v.endsWith(extension))
                .map(p -> p.substring(0, p.length() - extension.length())).toList()));
        } catch (IOException e) {
            conn.send(packetCreator.apply(List.of()));
        }
    }

    @Override
    public void handleGetCosmetic(CosmeticType cosmeticType, String name, Connection conn) {
        if (CommonUtil.isInvalidFilename(name)) {
            conn.send(new CosmeticReplyPacket(cosmeticType, name, ""));
            return;
        }
        Path path = cosmeticType.getPath(cosmeticsDirectory, name);
        try {
            String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
            conn.send(new CosmeticReplyPacket(cosmeticType, name, base64));
        } catch (IOException e) {
            conn.send(new CosmeticReplyPacket(cosmeticType, name, ""));
        }
    }

    private void modifyPlayerCosmetics(UUID id,
                                       Function<CosmeticMapConfigValue.PlayerCosmetics,
                                           CosmeticMapConfigValue.PlayerCosmetics> modify) {
        server.schedule(() -> {
            Configs.serverConfig.cosmeticMap.get().compute(id, (ignored, cosmetics) -> modify.apply(
                cosmetics == null ? new CosmeticMapConfigValue.PlayerCosmetics() : cosmetics));
            Configs.serverConfig.cosmeticMap.save();
            server.broadcast(new ReloadCosmeticForPlayer(id));
        });
    }

    @Override
    public void handleSetCape(Connection conn, @Nullable String capeName, boolean isMCCapes) {
        if (conn.getPlayerId() == null) return;
        modifyPlayerCosmetics(conn.getPlayerId(), cosmetics -> cosmetics.updateCape(capeName, isMCCapes));
    }

    @Override
    public void handleSetModels(Connection conn, List<String> models) {
        if (conn.getPlayerId() == null) return;
        modifyPlayerCosmetics(conn.getPlayerId(), cosmetics -> cosmetics.setModels(models));
    }

    @Override
    public void handleAddModel(Connection conn, String model) {
        if (conn.getPlayerId() == null) return;
        modifyPlayerCosmetics(conn.getPlayerId(), cosmetics -> cosmetics.addModel(model));
    }

    @Override
    public void handleRemoveModel(Connection conn, String model) {
        if (conn.getPlayerId() == null) return;
        modifyPlayerCosmetics(conn.getPlayerId(), cosmetics -> cosmetics.removeModel(model));
    }

    @Override
    public void handleGetCosmeticForPlayer(UUID playerId, Connection conn) {
        var value = Configs.serverConfig.cosmeticMap.get().get(playerId);
        if (value != null) conn.send(
            new GetCosmeticForPlayerReply(playerId, value.cape, value.capeIsMCCapes, value.models.stream().toList()));
        else conn.send(new GetCosmeticForPlayerReply(playerId, null, false, List.of()));
    }

    @Override
    public void onDisconnect(String reason, Connection connection) {
    }
}
