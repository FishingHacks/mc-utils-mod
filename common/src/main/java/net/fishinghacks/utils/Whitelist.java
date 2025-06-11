package net.fishinghacks.utils;

import com.mojang.authlib.GameProfile;
import net.fishinghacks.utils.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class Whitelist {
    public static final Component NOT_WHITELISTED = Component.translatable("multiplayer.disconnect.not_whitelisted");

    public static void kickUnlistedPlayers() {
        if (isDisabled()) return;
        var singleplayerServer = Minecraft.getInstance().getSingleplayerServer();
        if (!Minecraft.getInstance().isLocalServer() || singleplayerServer == null)
            throw new IllegalStateException("Only allowed in LAN worlds");
        for (var player : singleplayerServer.getPlayerList().getPlayers())
            if (!isWhitelisted(player.getGameProfile())) player.connection.disconnect(NOT_WHITELISTED);
    }

    public static void onPlayerJoin(ServerPlayer player) {
        if (isDisabled()) return;
        if (isWhitelisted(player.getGameProfile())) return;
        player.connection.disconnect(NOT_WHITELISTED);
    }

    public static boolean isWhitelisted(GameProfile profile) {
        return profile.equals(Minecraft.getInstance().getGameProfile()) || isWhitelisted(profile.getName());
    }

    public static boolean isWhitelisted(String playerName) {
        return Minecraft.getInstance().getGameProfile().getName()
            .equals(playerName) || Configs.whitelist.WHITELISTED_PLAYERS.get().stream().anyMatch(playerName::equals);
    }

    public static void add(String playerName) {
        Set<String> set = new HashSet<>(Configs.whitelist.WHITELISTED_PLAYERS.get());
        set.add(playerName);
        Configs.whitelist.WHITELISTED_PLAYERS.set(set.stream().toList());
    }

    public static void add(GameProfile profile) {
        add(profile.getName());
    }

    public static void remove(String playerName) {
        Set<String> set = new HashSet<>(Configs.whitelist.WHITELISTED_PLAYERS.get());
        set.remove(playerName);
        Configs.whitelist.WHITELISTED_PLAYERS.set(set.stream().toList());
    }

    public static void remove(GameProfile profile) {
        remove(profile.getName());
    }

    public static void setEnabled(boolean enabled) {
        Configs.whitelist.ENABLED.set(enabled);
    }

    public static boolean isDisabled() {
        return !Configs.whitelist.ENABLED.get();
    }
}
