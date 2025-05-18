package net.fishinghacks.utils.config;

import net.fishinghacks.utils.config.spec.Config;

import java.util.function.BiConsumer;

public class Configs {
    public static final ClientConfig clientConfig = new ClientConfig();
    public static final ServerConfig serverConfig = new ServerConfig();

    public static <T> void register(T value, BiConsumer<T, Config> onRegister) {
        register(value, onRegister, clientConfig, serverConfig);
    }

    public static <T> void register(T value, BiConsumer<T, Config> onRegister, Config... configs) {
        for (Config config : configs) {
            onRegister.accept(value, config);
        }
    }
}
