package net.fishinghacks.utils.client;

import com.google.common.base.Suppliers;
import net.fishinghacks.utils.client.connection.ClientConnectionHandler;
import net.fishinghacks.utils.client.gui.MainScreen;
import net.fishinghacks.utils.client.modules.misc.Zoom;
import net.fishinghacks.utils.common.Utils;
import net.fishinghacks.utils.client.caching.DownloadTextureCache;
import net.fishinghacks.utils.client.commands.CommandManager;
import net.fishinghacks.utils.client.gui.GuiOverlayManager;
import net.fishinghacks.utils.client.gui.configuration.ConfigurationSelectionScreen;
import net.fishinghacks.utils.client.modules.ClickUi;
import net.fishinghacks.utils.client.modules.DragUI;
import net.fishinghacks.utils.client.modules.ModuleManager;
import net.fishinghacks.utils.client.modules.RenderableModule;
import net.fishinghacks.utils.common.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.nio.file.Path;
import java.util.function.Supplier;

@Mod(value = UtilsClient.MODID, dist = Dist.CLIENT)
public class UtilsClient {
    public static final String MODID = Utils.MODID;
    public static final Supplier<Path> dataDirectory = Suppliers.memoize(
        () -> Minecraft.getInstance().gameDirectory.toPath().resolve(String.format(".%s_data", MODID)));

    public UtilsClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationSelectionScreen::new);
        NeoForge.EVENT_BUS.addListener(CommandManager::on_chat);
        NeoForge.EVENT_BUS.addListener(DownloadTextureCache::loadCaches);
        NeoForge.EVENT_BUS.addListener(Zoom::onScroll);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with
    // @SubscribeEvent
    @EventBusSubscriber(modid = UtilsClient.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(ClickUi.CLICK_UI_MAPPING.get());
        }

        @SubscribeEvent
        public static void registerGuiRender(RegisterGuiLayersEvent event) {
            event.registerAboveAll(Utils.id("gui_overlay"), (guiGraphics, delta) -> {
                float partialTick = delta.getGameTimeDeltaPartialTick(true);
                if (Minecraft.getInstance().screen instanceof DragUI) return;
                ModuleManager.enabledModules.forEach(mod -> {
                    if (ModuleManager.modules.get(mod) instanceof RenderableModule module)
                        module.render(guiGraphics, partialTick);
                });
            });
            event.registerAboveAll(Utils.id("notifications"), ((guiGraphics, deltaTracker) -> {
                if (Minecraft.getInstance().screen == null) GuiOverlayManager.renderNotifications(guiGraphics, -1, -1,
                    deltaTracker.getGameTimeDeltaPartialTick(true));
            }));
        }
    }

    @EventBusSubscriber(modid = UtilsClient.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onTick(ClientTickEvent.Post ignored) {
            if (ClickUi.CLICK_UI_MAPPING.get().consumeClick())
                Minecraft.getInstance().setScreen(new ClickUi(Minecraft.getInstance().screen));
            ClientConnectionHandler.getInstance().tick();
        }

        @SubscribeEvent
        public static void onWorldUnload(LevelEvent.Unload ignored) {
            ModuleManager.disableModule("freezecam");
            ModuleManager.disableModule("freecam");
            ModuleManager.disableModule("zoom");
        }
    }

    public static void onFirstGuiRender() {
        if (!Configs.clientConfig.AUTOCONNECT.get()) return;
        var list = Configs.clientConfig.SERVICE_SERVER_HISTORY.get();
        if (list.isEmpty()) return;
        var addr = ClientConnectionHandler.parseAddress(list.getFirst());
        if (addr == null) return;
        ClientConnectionHandler.getInstance().connect(addr);
    }

    public static Screen mainScreen() {
        if (Configs.clientConfig.CUSTOM_MENUS.get()) return new MainScreen();
        else return new TitleScreen();
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
