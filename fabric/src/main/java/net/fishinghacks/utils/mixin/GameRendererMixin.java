package net.fishinghacks.utils.mixin;

import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;" +
        "renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    public void renderWithTooltip(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!GuiOverlayManager.renderPre(guiGraphics, mouseX, mouseY, partialTick, screen))
            screen.renderWithTooltip(guiGraphics, mouseX, mouseY, partialTick);
        GuiOverlayManager.renderPost(guiGraphics, mouseX, mouseY, partialTick, screen);
    }
}
