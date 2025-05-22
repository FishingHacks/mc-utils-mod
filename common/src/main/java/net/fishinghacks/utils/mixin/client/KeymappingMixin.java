package net.fishinghacks.utils.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fishinghacks.utils.actions.Action;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class KeymappingMixin {
    @Inject(method = "set", cancellable = true, at = @At("HEAD"))
    private static void set(InputConstants.Key key, boolean held, CallbackInfo ci) {
        if (Action.onPress(key, held)) ci.cancel();
    }
}
