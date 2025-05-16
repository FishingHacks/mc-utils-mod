package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
