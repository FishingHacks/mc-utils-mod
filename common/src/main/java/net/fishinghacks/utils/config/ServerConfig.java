package net.fishinghacks.utils.config;

import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.spec.ConfigSpec;
import net.fishinghacks.utils.config.spec.ConfigType;
import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.config.values.CosmeticMapConfigValue;
import net.fishinghacks.utils.platform.services.IConfig;
import org.jetbrains.annotations.Nullable;

public class ServerConfig extends AbstractConfig {
    private final IConfig config;
    private final ConfigSpec spec;

    public final CachedValue<Boolean> serverEnabled;
    public final CachedValue<Integer> serverPort;
    public final CachedValue<String> serverName;
    public final CachedValue<Boolean> sendServerInvite;
    public final CachedValue<String> serverInviteName;
    public final CachedValue<String> serverInviteUrl;
    public final CosmeticMapConfigValue cosmeticMap;

    ServerConfig() {
        var builder = new ConfigBuilder();

        builder.worldRestart();
        serverEnabled = CachedValue.wrap(this, builder, "server_enabled", true);
        builder.worldRestart();
        serverPort = CachedValue.wrap(this, builder, "server_port", 25560);
        builder.worldRestart();
        serverName = CachedValue.wrap(this, builder, "server_name", "");
        builder.worldRestart();
        sendServerInvite = CachedValue.wrap(this, builder, "send_server_invite", false);
        builder.worldRestart();
        serverInviteName = CachedValue.wrap(this, builder, "server_invite_name", "");
        builder.worldRestart();
        serverInviteUrl = CachedValue.wrap(this, builder, "server_invite_url", "");
        builder.worldRestart();
        cosmeticMap = CosmeticMapConfigValue.wrap(this, builder, "cosmetic_map");

        spec = builder.getSpec();
        config = builder.build();
    }

    @Override
    public IConfig getConfig() {
        return config;
    }

    @Override
    public @Nullable String getFilename() {
        return null;
    }

    @Override
    public ConfigType type() {
        return ConfigType.Server;
    }

    @Override
    public ConfigSpec spec() {
        return spec;
    }
}
