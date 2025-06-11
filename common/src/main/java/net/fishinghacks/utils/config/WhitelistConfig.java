package net.fishinghacks.utils.config;

import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.spec.ConfigSpec;
import net.fishinghacks.utils.config.spec.ConfigType;
import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.platform.services.IConfig;

import java.util.*;

public class WhitelistConfig extends AbstractConfig {
    private final IConfig config;
    private final ConfigSpec spec;
    public final CachedValue<List<? extends String>> WHITELISTED_PLAYERS;
    public final CachedValue<Boolean> ENABLED;

    WhitelistConfig() {
        var builder = new ConfigBuilder();

        ENABLED = CachedValue.wrap(this, builder, "enabled", true);
        WHITELISTED_PLAYERS = CachedValue.wrapListEmpty(this, builder, "whitelist", () -> "", v -> v instanceof String);

        this.config = builder.build();
        this.spec = builder.getSpec();
    }

    @Override
    public IConfig getConfig() {
        return config;
    }

    @Override
    public ConfigType type() {
        return ConfigType.Client;
    }

    @Override
    public ConfigSpec spec() {
        return spec;
    }

    @Override
    public String getFilename() {
        return "utils-whitelist.toml";
    }

}
