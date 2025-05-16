package net.fishinghacks.utils;

import com.google.common.base.Suppliers;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.MainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.nio.file.Path;
import java.util.function.Supplier;

public class ClientConstants {
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

    public static final Supplier<Path> dataDirectory = Suppliers.memoize(
        () -> Minecraft.getInstance().gameDirectory.toPath().resolve(String.format(".%s_data", Constants.MOD_ID)));

}
