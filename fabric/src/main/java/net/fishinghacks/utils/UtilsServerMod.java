package net.fishinghacks.utils;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fishinghacks.utils.config.spec.ConfigType;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.config.ConfigsImpl;
import net.fishinghacks.utils.server.Server;
import net.minecraft.server.dedicated.DedicatedServer;

public class UtilsServerMod implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(
            server -> ConfigsImpl.registerConfigs(server.getServerDirectory().resolve("config"), ConfigType::isServer));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!Configs.serverConfig.serverEnabled.get() || !server.isDedicatedServer() || !(server instanceof DedicatedServer dedicatedServer))
                return;
            Server.initialize(dedicatedServer);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(ignored -> {
            if (Server.getInstance() != null) Server.getInstance().close();
        });
    }
}
