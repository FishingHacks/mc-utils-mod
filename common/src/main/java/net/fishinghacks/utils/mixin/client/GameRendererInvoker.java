package net.fishinghacks.utils.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
    @Invoker("getNightVisionScale")
    static float doGetNightVisionScale(LivingEntity entity, float partialTick) {
        throw new AssertionError();
    }
}
