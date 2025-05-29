package net.fishinghacks.utils.commands.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.cosmetics.CosmeticHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.status.ServerStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class ReloadCosmeticsCommand extends Command {
    public ReloadCosmeticsCommand() {
        super("reload_cosmetics");
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.executes(ignored -> reload_all())
            .then(argument("player", StringArgumentType.string()).executes(this::reload_player));
    }

    private int reload_all() {
        CosmeticHandler.reloadCosmetics();
        Minecraft.getInstance().getChatListener().handleSystemMessage(Translation.CosmeticReloadAll.get(), false);
        return SINGLE_SUCCESS;
    }

    @SuppressWarnings("SameReturnValue")
    private int reload_player(CommandContext<SharedSuggestionProvider> context) {
        String player_name = context.getArgument("player", String.class);
        var listener = Minecraft.getInstance().getChatListener();
        try {
            UUID uuid = UUID.fromString(player_name);
            // see above
            CosmeticHandler.reloadCosmeticsForPlayer(uuid);
            var info = Minecraft.getInstance().getConnection() != null ? Minecraft.getInstance().getConnection()
                .getPlayerInfo(uuid) : null;
            if (info != null) {
                var profile = info.getProfile();
                listener.handleSystemMessage(Translation.CosmeticReloadPlayer.with(profile.getName(), profile.getId()),
                    false);
            } else listener.handleSystemMessage(Translation.CosmeticReloadUUID.with(uuid), false);
            return SINGLE_SUCCESS;
        } catch (IllegalArgumentException ignored) {
        }
        @Nullable GameProfile profile = null;
        ServerData data = Minecraft.getInstance().getCurrentServer();
        if (data != null) {
            ServerStatus.Players players = data.players;
            if (players == null) {
                listener.handleSystemMessage(Translation.NoPlayerFound.with(player_name), false);
                return SINGLE_SUCCESS;
            }
            Optional<GameProfile> player = data.players.sample().stream()
                .filter((p) -> p.getName().equalsIgnoreCase(player_name)).findFirst();
            if (player.isPresent()) profile = player.get();
        }

        if (profile == null) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) throw new IllegalStateException("No server connection or client leve, but in-game");
            Optional<GameProfile> player = level.players().stream().map(AbstractClientPlayer::getGameProfile)
                .filter((p) -> p.getName().equalsIgnoreCase(player_name)).findFirst();
            if (player.isEmpty()) {
                listener.handleSystemMessage(Translation.NoPlayerFound.with(player_name), false);
                return SINGLE_SUCCESS;
            }
            profile = player.get();
        }
        // see above
        CosmeticHandler.reloadCosmeticsForPlayer(profile.getId());
        listener.handleSystemMessage(Translation.CosmeticReloadPlayer.with(profile.getName(), profile.getId()), false);
        return SINGLE_SUCCESS;
    }
}
