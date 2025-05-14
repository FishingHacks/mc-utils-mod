package net.fishinghacks.utils.mixins.client;

import net.fishinghacks.utils.client.modules.ModuleManager;
import net.fishinghacks.utils.client.modules.misc.Freecam;
import net.fishinghacks.utils.client.modules.misc.Freezecam;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow
    private boolean initialized;
    @Shadow
    private BlockGetter level;
    @Shadow
    private Entity entity;
    @Shadow
    private boolean detached;
    @Shadow
    private float partialTickTime;
    @Shadow
    private float eyeHeightOld;
    @Shadow
    private float eyeHeight;

    @Inject(method = "setup", cancellable = true, at = @At("HEAD"))
    public void setup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick,
                      CallbackInfo ci) {
        if (!Freezecam.isEnabled && !Freecam.isEnabled) return;
        if(!detached) {
            ModuleManager.disableModule("freecam");
            ModuleManager.disableModule("freezecam");
            return;
        }
        this.initialized = true;
        this.level = level;
        this.entity = entity;
        this.detached = true;
        this.partialTickTime = partialTick;
        ci.cancel();

        if (Freecam.isEnabled) {
            ((CameraInvoker) this).invokeSetPosition(Mth.lerp(partialTick, entity.xo, entity.getX()),
                Mth.lerp(partialTick, entity.yo, entity.getY()) + Mth.lerp(partialTick, this.eyeHeightOld,
                    this.eyeHeight), Mth.lerp(partialTick, entity.zo, entity.getZ()));
            float scale = entity instanceof LivingEntity livingentity ? livingentity.getScale() : 1.0F;
            //noinspection DataFlowIssue
            ((CameraInvoker) this).invokeMove(-((CameraInvoker) this).invokeGetMaxZoom(
                net.neoforged.neoforge.client.ClientHooks.getDetachedCameraDistance((Camera) (Object) this,
                    thirdPersonReverse, scale, 4f) * scale), 0f, 0f);
        }
    }
}


