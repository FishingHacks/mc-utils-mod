package net.fishinghacks.utils.common;

import net.fishinghacks.utils.common.config.Configs;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(value = Utils.MODID)
public class Utils {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "utils";

    public static Logger getLOGGER() {
        return LOGGER;
    }

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static ModContainer container;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Utils(IEventBus modEventBus, ModContainer modContainer) {
        container = modContainer;
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Utils) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like
        // onServerStarting() below.
        modEventBus.addListener(Configs::onConfigLoad);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        Configs.register(modContainer);
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
