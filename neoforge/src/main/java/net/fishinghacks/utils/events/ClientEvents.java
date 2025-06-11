package net.fishinghacks.utils.events;

import net.fishinghacks.utils.*;
import net.fishinghacks.utils.caching.DownloadTextureCache;
import net.fishinghacks.utils.commands.CommandManager;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.fishinghacks.utils.modules.ClickUi;
import net.fishinghacks.utils.modules.ModuleManager;
import net.fishinghacks.utils.modules.misc.Zoom;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut ignored) {
        E4MCStore.onDisconnect();
        ModuleManager.onServerLeave();
        ModDisabler.onServerLeave();
    }

    @SubscribeEvent
    public static void onConnect(ClientPlayerNetworkEvent.LoggingIn ev) {
        E4MCStore.onConnect();
        if (!ev.getConnection().isMemoryConnection()) ModDisabler.onServerJoin();
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent e) {
        E4MCStore.onCommandExecuted(e.getParseResults());
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent e) {
        e.setCanceled(Zoom.onScroll((float) e.getScrollDeltaY()));
    }

    @SubscribeEvent
    public static void onRenderPre(ScreenEvent.Render.Pre e) {
        e.setCanceled(GuiOverlayManager.renderPre(e.getGuiGraphics(), e.getMouseX(), e.getMouseY(), e.getPartialTick(),
            e.getScreen()));
    }

    @SubscribeEvent
    public static void onRenderPost(ScreenEvent.Render.Post e) {
        GuiOverlayManager.renderPost(e.getGuiGraphics(), e.getMouseX(), e.getMouseY(), e.getPartialTick(),
            e.getScreen());
    }

    @SubscribeEvent
    public static void onMousePressedPre(ScreenEvent.MouseButtonPressed.Pre e) {
        e.setCanceled(GuiOverlayManager.onClick((int) e.getMouseX(), (int) e.getMouseY(), e.getButton()));
    }

    @SubscribeEvent
    public static void onMouseDraggedPre(ScreenEvent.MouseDragged.Pre e) {
        e.setCanceled(
            GuiOverlayManager.onDrag((int) e.getMouseX(), (int) e.getMouseY(), e.getMouseButton(), e.getDragX(),
                e.getDragY()));
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post ignored) {
        if (ClickUi.CLICK_UI_MAPPING.get().consumeClick())
            Minecraft.getInstance().setScreen(new ClickUi(Minecraft.getInstance().screen));
        ClientConnectionHandler.getInstance().tick();
    }

    @SubscribeEvent
    public static void onChat(ClientChatEvent e) {
        e.setCanceled(CommandManager.onChat(e.getMessage()));
    }

    @SubscribeEvent
    public static void onStarted(ClientStartedEvent ignored) {
        Telemetry.start("Overlay Manager");
        DownloadTextureCache.loadCaches();
        CommandManager.init();
    }

    @SubscribeEvent
    public static void onStopping(ClientStoppingEvent ignored) {
        Telemetry.stop("Overlay Manager");
        Telemetry.shutdown();
    }

    @SubscribeEvent
    public static void onToast(ToastAddEvent event) {
        event.setCanceled(GuiOverlayManager.onToast(event.getToast()));
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent ev) {
        if (ev.getEntity() instanceof ServerPlayer p) Whitelist.onPlayerJoin(p);
    }
}
