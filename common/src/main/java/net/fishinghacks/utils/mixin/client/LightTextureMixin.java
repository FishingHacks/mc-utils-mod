package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.modules.misc.Fullbright;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    @Redirect(method = "updateLightTexture", at = @At(value = "INVOKE", target = "hasEffect"))
    public boolean hasEffect(LocalPlayer self, Holder<MobEffect> effectHolder) {
        if (effectHolder.equals(MobEffects.NIGHT_VISION) && Fullbright.isEnabled) return true;
        return self.hasEffect(effectHolder);
    }

    @Redirect(method = "updateLightTexture", at = @At(value = "INVOKE", target = "getNightVisionScale"))
    public float getNightVisionScale(LivingEntity entity, float partialTick) {
        if (Fullbright.isEnabled) return 1f;
        return GameRendererInvoker.doGetNightVisionScale(entity, partialTick);
    }

    @Mixin(GameRenderer.class)
    public interface GameRendererInvoker {
        @Invoker
        static float doGetNightVisionScale(LivingEntity entity, float partialTick) {
            throw new AssertionError();
        }
    }
}
