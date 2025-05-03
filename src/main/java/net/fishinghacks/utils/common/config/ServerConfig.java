package net.fishinghacks.utils.common.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerConfig extends Config {
    private final ModConfigSpec configSpec;

    public final CachedValue<Boolean> serverEnabled;
    public final CachedValue<Integer> serverPort;
    public final CachedValue<String> serverName;
    public final CachedValue<Boolean> sendServerInvite;
    public final CachedValue<String> serverInviteName;
    public final CachedValue<String> serverInviteUrl;
    public final CosmeticMapConfigValue cosmeticMap;

    ServerConfig() {
        var builder = new ModConfigSpec.Builder();

        serverEnabled = CachedValue.wrap(this, builder.worldRestart().define("server_enabled", true));
        serverPort = CachedValue.wrap(this, builder.worldRestart().define("server_port", 25560));
        serverName = CachedValue.wrap(this, builder.define("server_name", ""));
        sendServerInvite = CachedValue.wrap(this, builder.worldRestart().define("send_server_invite", false));
        serverInviteName = CachedValue.wrap(this, builder.worldRestart().define("server_invite_name", ""));
        serverInviteUrl = CachedValue.wrap(this, builder.worldRestart().define("server_invite_url", ""));
        cosmeticMap = new CosmeticMapConfigValue(builder.define("cosmetic_map", "{}"), this);

        configSpec = builder.build();
    }

    @Override
    public @Nullable String getFilename() {
        return null;
    }

    @Override
    public @NotNull ModConfigSpec getModConfigSpec() {
        return configSpec;
    }

    @Override
    public ModConfig.Type getConfigType() {
        return ModConfig.Type.SERVER;
    }
}
