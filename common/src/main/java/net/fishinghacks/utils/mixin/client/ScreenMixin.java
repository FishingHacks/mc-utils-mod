package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.Telemetry;
import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void overlayMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        var overlay = GuiOverlayManager.getOverlay();
        if (overlay == null) return;
        if (overlay.rectangle().containsPoint((int) mouseX, (int) mouseY)) {
            overlay.owner().mouseClicked(mouseX, mouseY, button);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void overlayMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        var overlay = GuiOverlayManager.getOverlay();
        if (overlay == null) return;
        if (overlay.rectangle().containsPoint((int) mouseX, (int) mouseY)) {
            overlay.owner().mouseReleased(mouseX, mouseY, button);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void overlayMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY,
                                     CallbackInfoReturnable<Boolean> ci) {
        if (button != 0) return;
        var overlay = GuiOverlayManager.getOverlay();
        if (overlay == null) return;
        if (overlay.rectangle().containsPoint((int) mouseX, (int) mouseY)) {
            overlay.owner().mouseDragged(mouseX, mouseY, button, dragX, dragY);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void overlayMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY,
                                      CallbackInfoReturnable<Boolean> ci) {
        var overlay = GuiOverlayManager.getOverlay();
        if (overlay == null) return;
        if (overlay.rectangle().containsPoint((int) mouseX, (int) mouseY)) {
            overlay.owner().mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "added", at = @At("HEAD"))
    private void onAdd(CallbackInfo ci) {
        var name = this.getClass().getName();
        Telemetry.start(name);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemove(CallbackInfo ci) {
        var name = this.getClass().getName();
        Telemetry.stop(name);
    }

    @Unique
    private long utils_mod_multiloader$renderStart = 0L;

    @Inject(method = "render", at = @At("HEAD"))
    private void renderStart(GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_, CallbackInfo ci) {
        utils_mod_multiloader$renderStart = Util.getMillis();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderStop(GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_, CallbackInfo ci) {
        var name = this.getClass().getName();
        Telemetry.registerRender(name, (int) (Util.getMillis() - utils_mod_multiloader$renderStart));
    }
}
