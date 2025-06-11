package net.fishinghacks.utils;

import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.fishinghacks.utils.gui.components.Notification;
import net.fishinghacks.utils.macros.ExecutionManager;
import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class ModDisabler {
    private static boolean enableOverride;
    // hacky stuff to make the linter shut up
    @Nullable
    @Contract
    public static <T> T id(@Nullable T v) {
        return v;
    }

    public static boolean isModDisabled() {
        return !enableOverride && id(Minecraft.getInstance()) != null && Minecraft.getInstance()
            .getCurrentServer() != null;
    }

    public static void enable() {
        ModuleManager.modules.values().forEach(IModule::updateEnabled);
        enableOverride = true;
    }

    public static void onServerJoin() {
        ModuleManager.modules.values().forEach(IModule::updateEnabled);
        enableOverride = false;

        ExecutionManager.stopAllMacros();

        GuiOverlayManager.addNotification(Translation.ServerWarning.get(),
            new Notification.NotifyButton(Translation.Enable.get(), (ignored, notification) -> {
                ModDisabler.enable();
                notification.close();
            }),
            new Notification.NotifyButton(CommonComponents.GUI_OK, (ignored, notification) -> notification.close()));
    }

    public static void onServerLeave() {
        ModuleManager.modules.values().forEach(IModule::updateEnabled);
        enableOverride = false;
    }
}
