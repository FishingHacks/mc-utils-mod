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

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
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
        ClientLifecycleEvents.CLIENT_STARTED.register(ignored -> DownloadTextureCache.loadCaches());

        Configs.register(null, (ignored0, ignored1) -> {
        });
    }
}
