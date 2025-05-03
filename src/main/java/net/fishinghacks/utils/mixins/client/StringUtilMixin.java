package net.fishinghacks.utils.mixins.client;

import net.fishinghacks.utils.client.modules.misc.AllowBlockedKeys;
import net.minecraft.util.StringUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringUtil.class)
public class StringUtilMixin {
    @Inject(method = "isAllowedChatCharacter", at = @At("HEAD"), cancellable = true)
    private static void allowIllegalChars(CallbackInfoReturnable<Boolean> ci) {
        if(AllowBlockedKeys.allowKeys) ci.setReturnValue(true);
    }
}
