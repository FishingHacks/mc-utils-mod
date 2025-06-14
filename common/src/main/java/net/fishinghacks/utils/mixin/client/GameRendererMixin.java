package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.modules.misc.Freezecam;
import net.fishinghacks.utils.modules.misc.Zoom;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", cancellable = true, at = @At("HEAD"))
    public void getFov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> ci) {
        if (Zoom.instance.isEnabled()) {
            ci.setReturnValue(Zoom.fov);
            return;
        }
        if (!Freezecam.instance.isEnabled()) return;
        ci.setReturnValue((float) Minecraft.getInstance().options.fov().get());
    }
}
