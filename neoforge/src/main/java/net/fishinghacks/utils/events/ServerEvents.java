package net.fishinghacks.utils.events;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.server.Server;
import net.minecraft.server.dedicated.DedicatedServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER, modid = Constants.MOD_ID)
public class ServerEvents {
    @SubscribeEvent
    public static void onStart(ServerStartedEvent event) {
        var server = event.getServer();
        if (!Configs.serverConfig.serverEnabled.get() || !server.isDedicatedServer() || !(server instanceof DedicatedServer dedicatedServer))
            return;
        Server.initialize(dedicatedServer);
    }

    @SubscribeEvent
    public static void onStop(ServerStoppingEvent ignored) {
        if (Server.getInstance() != null) Server.getInstance().close();
    }
}
