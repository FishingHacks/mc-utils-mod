package net.fishinghacks.utils.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.modules.misc.MufflerModule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @ModifyArg(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds" +
        "/SimpleSoundInstance;<init>(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFLnet" +
        "/minecraft/util/RandomSource;DDD)V"), index = 2)
    float a(float volume, @Local(argsOnly = true) SoundEvent soundEvent) {
        if (!MufflerModule.isEnabled) return volume;
        Integer mufflerVolume = Configs.clientConfig.MUFFLER_STATE.get().get(soundEvent.location());
        if (mufflerVolume == null) return volume;
        if (mufflerVolume < 1) return 0f;
        return volume * ((float) mufflerVolume / 100f);
    }
}
