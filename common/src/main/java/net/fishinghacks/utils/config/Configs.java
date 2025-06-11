package net.fishinghacks.utils.config;

import net.fishinghacks.utils.config.spec.AbstractConfig;

import java.util.function.BiConsumer;

public class Configs {
    public static final ClientConfig clientConfig = new ClientConfig();
    public static final ServerConfig serverConfig = new ServerConfig();
    public static final WhitelistConfig whitelist = new WhitelistConfig();

    public static <T> void register(T value, BiConsumer<T, AbstractConfig> onRegister) {
        register(value, onRegister, clientConfig, serverConfig, whitelist);
    }

    public static <T> void register(T value, BiConsumer<T, AbstractConfig> onRegister, AbstractConfig... configs) {
        for (AbstractConfig config : configs) {
            onRegister.accept(value, config);
        }
    }
}
