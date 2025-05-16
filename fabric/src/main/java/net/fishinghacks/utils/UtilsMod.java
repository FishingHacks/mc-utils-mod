package net.fishinghacks.utils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fishinghacks.utils.caching.DownloadTextureCache;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.modules.ClickUi;
import net.fishinghacks.utils.modules.ModuleManager;

public class UtilsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Configs.register(null, (ignored0, ignored1) -> {
        });
    }
}
