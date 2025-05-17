package net.fishinghacks.utils.config;

import net.fishinghacks.utils.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ConfigsImpl {
    private static final Map<IConfigSpec, Config> KNOWN_CONFIGS = new HashMap<>();

    public static void register(ModContainer modContainer) {
        Configs.register(modContainer, ConfigsImpl::register);
    }

    public static void register(ModContainer modContainer, Config config) {
        String filename = config.getFilename();
        if(filename == null) filename = Constants.MOD_ID + "-" + config.type().extension() + ".toml";
        ModConfigSpec spec = ((ConfigImpl) config.getConfig()).inner();
        var type = switch (config.type()) {
            case Client -> ModConfig.Type.CLIENT;
            case Server -> ModConfig.Type.SERVER;
        };
        modContainer.registerConfig(type, spec, filename);
        KNOWN_CONFIGS.put(spec, config);
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Unloading) return;
        ModConfig eventConfig = event.getConfig();
        if (!eventConfig.getModId().equals(Constants.MOD_ID)) return;
        Config config = KNOWN_CONFIGS.get(eventConfig.getSpec());
        if (config != null) config.clearCache();
    }
}
