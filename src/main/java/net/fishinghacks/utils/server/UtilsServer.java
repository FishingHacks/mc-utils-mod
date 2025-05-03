package net.fishinghacks.utils.server;

import com.mojang.logging.LogUtils;
import net.fishinghacks.utils.common.Utils;
import net.fishinghacks.utils.common.config.Configs;
import net.fishinghacks.utils.server.server.Server;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;

@Mod(value = UtilsServer.MODID, dist = Dist.CLIENT)
public class UtilsServer {
    public static final String MODID = Utils.MODID;
    @Nullable
    private static Server server = null;
    @Nullable
    private static MinecraftServer mcserver;
    public static final Logger logger = LogUtils.getLogger();

    public UtilsServer(ModContainer modContainer) {
    }

    @EventBusSubscriber(modid = UtilsServer.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.DEDICATED_SERVER)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onStart(ServerStartedEvent event) {
            mcserver = event.getServer();
            if (!Configs.serverConfig.serverEnabled.get()) return;
            if (!event.getServer().isDedicatedServer()) return;
            if (!(event.getServer() instanceof DedicatedServer dedicatedServer)) return;
            server = new Server(dedicatedServer);
            server.run();
        }

        @SubscribeEvent
        public static void onStop(ServerStoppingEvent ignored) {
            if (server != null) server.close();
        }
    }

    public static void tickServer() {
        if (server != null) server.tick();
    }

    @NotNull
    public static MinecraftServer getMcServer() {
        assert mcserver != null;
        return mcserver;
    }

    @Nullable
    public static Server getServer() {
        return server;
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
