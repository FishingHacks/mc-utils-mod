package net.fishinghacks.utils.mixin;

import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.fishinghacks.utils.modules.misc.Zoom;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Redirect(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;" +
        "mouseClicked(DDI)Z"))
    public boolean mouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        if (GuiOverlayManager.onClick((int) mouseX, (int) mouseY, button)) return true;
        return screen.mouseClicked(mouseX, mouseY, button);
    }

    @Redirect(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;" +
        "mouseScrolled(DDDD)Z"))
    public boolean mouseScrolled(Screen screen, double mouseX, double mouseY, double scrollX, double scrollY) {
        if (Zoom.onScroll((float) scrollY)) return true;
        return screen.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
