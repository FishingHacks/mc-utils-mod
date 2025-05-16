package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.modules.misc.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "turn"))
    public void turn(LocalPlayer player, double y, double x) {
        if (!Freecam.isEnabled) {
            player.turn(y, x);
            return;
        }

        float xRot = (float) x * 0.15f;
        float yRot = (float) y * 0.15f;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        xRot += camera.getXRot();
        yRot += camera.getYRot();
        xRot = Math.clamp(xRot, -90f, 90f);
        ((CameraInvoker) camera).invokeSetRotation(yRot, xRot);
    }
}
