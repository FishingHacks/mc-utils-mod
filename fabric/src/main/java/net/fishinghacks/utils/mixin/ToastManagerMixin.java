package net.fishinghacks.utils.mixin;

import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class ToastManagerMixin {
    @Inject(method = "addToast", cancellable = true, at = @At("HEAD"))
    public void addToast(Toast toast, CallbackInfo ci) {
        if(GuiOverlayManager.onToast(toast)) ci.cancel();
    }
}
