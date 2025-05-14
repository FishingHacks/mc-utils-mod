package net.fishinghacks.utils.mixins.client;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraInvoker {
    @Invoker
    void invokeSetPosition(double x, double y, double z);

    @Invoker
    void invokeMove(float zoom, float dy, float dx);

    @Invoker
    float invokeGetMaxZoom(float maxZoom);

    @Invoker
    void invokeSetRotation(float y, float x, float roll);
}
