package net.fishinghacks.utils.client.commands;

import com.mojang.authlib.GameProfile;
import net.fishinghacks.utils.client.cosmetics.CosmeticHandler;
import net.fishinghacks.utils.common.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.status.ServerStatus;

import java.util.Optional;
import java.util.UUID;

public class ReloadCosmeticsCommand extends DotCommand {
    @Override
    public String getName() {
        return "reload_cosmetics";
    }

    @Override
    public void run(String args, ChatListener listener) {
        if (args.isEmpty()) {
            // it is fine to do this because the rendering logic will try to get the profile, resulting in a "reload".
            CosmeticHandler.reloadCosmetics();
            listener.handleSystemMessage(Translation.CosmeticReloadAll.get(), false);
            return;
        }
        try {
            UUID uuid = UUID.fromString(args);
            // see above
            CosmeticHandler.reloadCosmeticsForPlayer(uuid);
            listener.handleSystemMessage(Translation.CosmeticReloadUUID.with(uuid), false);
            return;
        } catch (IllegalArgumentException ignored) {
        }
        // try to get the uuid from the name
        ServerData data = Minecraft.getInstance().getCurrentServer();
        if (data == null) {
            ClientLevel level = Minecraft.getInstance().level;
            if(level == null)
                throw new IllegalStateException("No server connection or client leve, but in-game");
            Optional<GameProfile> player = level.players().stream().map(AbstractClientPlayer::getGameProfile).filter((profile) -> profile.getName().equalsIgnoreCase(args)).findFirst();
            if (player.isEmpty()) {
                listener.handleSystemMessage(Translation.NoPlayerFound.with(args), false);
                return;
            }
            GameProfile profile = player.get();
            // see above
            CosmeticHandler.reloadCosmeticsForPlayer(profile.getId());
            listener.handleSystemMessage(Translation.CosmeticReloadPlayer.with(profile.getName(), profile.getId()), false);
            return;
        }
        ServerStatus.Players players = data.players;
        if (players == null) {
            listener.handleSystemMessage(Translation.NoPlayerFound.with(args), false);
            return;
        }
        Optional<GameProfile> player = data.players.sample().stream().filter((profile) -> profile.getName().equalsIgnoreCase(args)).findFirst();
        if (player.isEmpty()) {
            listener.handleSystemMessage(Translation.NoPlayerFound.with(args), false);
            return;
        }
        GameProfile profile = player.get();
        // see above
        CosmeticHandler.reloadCosmeticsForPlayer(profile.getId());
        listener.handleSystemMessage(Translation.CosmeticReloadPlayer.with(profile.getName(), profile.getId()), false);
    }
}
