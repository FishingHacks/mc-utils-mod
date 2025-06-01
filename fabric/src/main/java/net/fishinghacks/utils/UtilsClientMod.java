package net.fishinghacks.utils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fishinghacks.utils.caching.DownloadTextureCache;
import net.fishinghacks.utils.commands.CommandManager;
import net.fishinghacks.utils.config.spec.ConfigType;
import net.fishinghacks.utils.config.ConfigsImpl;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.fishinghacks.utils.modules.ClickUi;
import net.fishinghacks.utils.modules.DragUI;
import net.fishinghacks.utils.modules.ModuleManager;
import net.fishinghacks.utils.modules.RenderableModule;
import net.minecraft.client.Minecraft;

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
            CommandManager.init();
        });
        HudLayerRegistrationCallback.EVENT.register(wrapper -> {
            wrapper.addLayer(IdentifiedLayer.of(Constants.id("gui_overlay"), (guiGraphics, delta) -> {
                float partialTick = delta.getGameTimeDeltaPartialTick(true);
                if (Minecraft.getInstance().screen instanceof DragUI) return;
                ModuleManager.enabledModules.forEach(mod -> {
                    if (ModuleManager.modules.get(mod) instanceof RenderableModule module)
                        if (module.shouldRender()) module.render(guiGraphics, partialTick);
                });
            }));
            wrapper.addLayer(IdentifiedLayer.of(Constants.id("notifications"), (guiGraphics, deltaTracker) -> {
                if (Minecraft.getInstance().screen == null) {
                    GuiOverlayManager.repositionNotifications(Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                        Minecraft.getInstance().getWindow().getGuiScaledHeight());
                    GuiOverlayManager.renderNotifications(guiGraphics, -1, -1,
                        deltaTracker.getGameTimeDeltaPartialTick(true));
                }
            }));
        });
        ServerPlayConnectionEvents.JOIN.register(
            (handler, sender, server) -> Whitelist.onPlayerJoin(handler.getPlayer()));
    }
}
