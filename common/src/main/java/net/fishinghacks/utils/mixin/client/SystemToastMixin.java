package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.fishinghacks.utils.gui.components.Notification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@Mixin(SystemToast.class)
public class SystemToastMixin {
    @Unique
    private static final HashMap<SystemToast.SystemToastId, Notification> utils_mod_multiloader$notifications =
        new HashMap<>();

    @Inject(method = "<init>(Lnet/minecraft/client/gui/components/toasts/SystemToast$SystemToastId;Lnet" +
        "/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;)V", at = @At("RETURN"))
    private void addOrUpdate(SystemToast.SystemToastId id, Component title, @Nullable Component message,
                             CallbackInfo ignored) {
        if (!Configs.clientConfig.REPLACE_SYSTEM_TOASTS.get()) return;
        if (utils_mod_multiloader$notifications.get(id) != null) utils_mod_multiloader$notifications.get(id).close();

        Component comp = message == null ? title : Component.empty().append(title).append(": ").append(message);
        var notification = GuiOverlayManager.addNotification(comp);
        utils_mod_multiloader$notifications.put(id, notification);
        notification.onClose(n -> utils_mod_multiloader$notifications.remove(id, n));
    }

    @Inject(method = "multiline", at = @At("HEAD"))
    private static void multiline(Minecraft ignored0, SystemToast.SystemToastId id, Component title, Component message,
                                  CallbackInfoReturnable<SystemToast> ignored1) {
        if (!Configs.clientConfig.REPLACE_SYSTEM_TOASTS.get()) return;
        Component comp = message == null ? title : Component.empty().append(title).append(": ").append(message);
        var notification = GuiOverlayManager.addNotification(comp);
        utils_mod_multiloader$notifications.put(id, notification);
        notification.onClose(n -> utils_mod_multiloader$notifications.remove(id, n));
    }
}
