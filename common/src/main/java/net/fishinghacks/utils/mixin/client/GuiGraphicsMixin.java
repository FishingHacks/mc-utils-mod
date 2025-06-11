package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.modules.misc.ItemRender;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow
    protected abstract void renderItemCount(Font font, ItemStack stack, int x, int y, @Nullable String text);

    @Shadow
    protected abstract void renderItemCooldown(ItemStack stack, int x, int y);

    @Shadow
    protected abstract void renderItemBar(ItemStack stack, int x, int y);

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;" +
        "IILjava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        if (!ItemRender.instance.isEnabled()) return;
        ci.cancel();
        if (stack.isEmpty()) return;
        GuiGraphics self = (GuiGraphics) (Object) this;
        self.pose().pushPose();
        switch (ItemRender.instance.durabilityDisplay.get()) {
            case Bar -> renderItemBar(stack, x, y);
            case Text -> utils_mod_multiloader$renderItemDurabilityText(font, stack, x + 2, y + 11);
            case Both -> {
                renderItemBar(stack, x, y);
                utils_mod_multiloader$renderItemDurabilityText(font, stack, x + 2, y + 9);
            }
            case None -> {
            }
        }
        if (ItemRender.instance.showAmount.get()) renderItemCount(font, stack, x, y, text);
        if (ItemRender.instance.showCooldown.get()) renderItemCooldown(stack, x, y);
        self.pose().popPose();
    }

    @Unique
    private void utils_mod_multiloader$renderItemDurabilityText(Font font, ItemStack stack, int x, int y) {
        if (!stack.isDamageableItem()) return;
        String percentageDurability =
            (int) (100f - ((float) stack.getDamageValue() / stack.getMaxDamage()) * 100f) + "%";
        GuiGraphics graphics = (GuiGraphics) (Object) this;
        graphics.pose().pushPose();
        graphics.pose().scale(.5f, .5f, 1f);
        graphics.pose().translate(0f, 0f, 200f);
        graphics.drawString(font, percentageDurability, x * 2, y * 2, ARGB.opaque(stack.getBarColor()));
        graphics.pose().popPose();
    }
}
