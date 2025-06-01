package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.modules.misc.ItemRender;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hasFoil", cancellable = true, at = @At("HEAD"))
    public void hasFoil(CallbackInfoReturnable<Boolean> cir) {
        if (ItemRender.instance.enabled && !ItemRender.instance.showEnchantmentGlint.get()) cir.setReturnValue(false);
    }
}
