package net.fishinghacks.utils;

import com.google.common.base.Suppliers;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.MainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.function.Supplier;

public class Constants {
    public static final String MOD_ID = "utils";
    public static final String MOD_NAME = "Utils";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static final Supplier<Path> dataDirectory = Suppliers.memoize(
        () -> Minecraft.getInstance().gameDirectory.toPath().resolve(String.format(".%s_data", MOD_ID)));

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static Screen mainScreen() {
        if (Configs.clientConfig.CUSTOM_MENUS.get()) return new MainScreen();
        else return new TitleScreen();
    }

    public static void onFirstGuiRender() {
        if (!Configs.clientConfig.AUTOCONNECT.get()) return;
        var list = Configs.clientConfig.SERVICE_SERVER_HISTORY.get();
        if (list.isEmpty()) return;
        var addr = ClientConnectionHandler.parseAddress(list.getFirst());
        if (addr == null) return;
        ClientConnectionHandler.getInstance().connect(addr);
    }
}