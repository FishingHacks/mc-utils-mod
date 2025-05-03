package net.fishinghacks.utils.common.config;

import net.fishinghacks.utils.common.Utils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

import java.util.HashMap;
import java.util.Map;

public class Configs {
    private static final Map<IConfigSpec, Config> KNOWN_CONFIGS = new HashMap<>();
    public static final ClientConfig clientConfig = new ClientConfig();
    public static final ServerConfig serverConfig = new ServerConfig();

    public static void register(ModContainer container) {
        register(container, clientConfig, serverConfig);
    }

    public static void register(ModContainer container, Config... configs) {
        for(Config config : configs) register(container, config);
    }

    public static void register(ModContainer container, Config config) {
        String filename = config.getFilename();
        if(filename == null) container.registerConfig(config.getConfigType(), config.getModConfigSpec());
        else container.registerConfig(config.getConfigType(), config.getModConfigSpec(), Utils.MODID + "/" + filename + ".toml");
        KNOWN_CONFIGS.put(config.getModConfigSpec(), config);
    }

    public static void onConfigLoad(ModConfigEvent configEvent) {
        if(configEvent instanceof ModConfigEvent.Unloading) return;
        ModConfig eventConfig = configEvent.getConfig();
        if (!eventConfig.getModId().equals(Utils.MODID)) return;
        Config config = KNOWN_CONFIGS.get(eventConfig.getSpec());
        if(config != null) config.clearCache();
    }
}
