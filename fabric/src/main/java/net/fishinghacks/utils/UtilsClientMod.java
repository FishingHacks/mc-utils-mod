package net.fishinghacks.utils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fishinghacks.utils.caching.DownloadTextureCache;
import net.fishinghacks.utils.config.ConfigType;
import net.fishinghacks.utils.config.ConfigsImpl;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.modules.ClickUi;
import net.fishinghacks.utils.modules.ModuleManager;

public class UtilsClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ServerPlayConnectionEvents.JOIN.register((i0, i1, i2) -> E4MCStore.onConnect());
        ServerPlayConnectionEvents.DISCONNECT.register((i1, i2) -> {
            E4MCStore.onDisconnect();
            ModuleManager.disableModule("freezecam");
            ModuleManager.disableModule("freecam");
            ModuleManager.disableModule("zoom");
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (ClickUi.CLICK_UI_MAPPING.get().consumeClick()) mc.setScreen(new ClickUi(mc.screen));
            ClientConnectionHandler.getInstance().tick();
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
            ConfigsImpl.registerConfigs(mc.gameDirectory.toPath().resolve("config"), ConfigType::isClient);
            DownloadTextureCache.loadCaches();
        });
    }
}
