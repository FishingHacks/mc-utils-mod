package net.fishinghacks.utils.mixins.client;

import net.fishinghacks.utils.client.modules.misc.Freezecam;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", cancellable = true, at = @At("HEAD"))
    public void getFov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> ci) {
        if (!Freezecam.isEnabled) return;
        ci.setReturnValue((float) Minecraft.getInstance().options.fov().get());
    }
}
