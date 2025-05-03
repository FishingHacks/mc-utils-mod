package net.fishinghacks.utils.mixins.client;

import net.fishinghacks.utils.client.E4MCStore;
import net.fishinghacks.utils.client.connection.ClientConnectionHandler;
import net.fishinghacks.utils.client.gui.Icons;
import net.fishinghacks.utils.client.gui.PauseMenuScreen;
import net.fishinghacks.utils.common.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {
    @Shadow
    private Button disconnectButton;
    private Button inviteButton;

    protected PauseScreenMixin(Component title) {
        super(title);
    }


    @Inject(method = "createPauseMenu", at = @At("TAIL"))
    public void createPauseMenu(CallbackInfo ci) {
        int posY = disconnectButton.getY() - 2 * Button.DEFAULT_HEIGHT - 2 * 4;
        int posX = width / 2 + 4 + 102;
        inviteButton = addRenderableWidget(
            new IconButton(posX, posY, Icons.INVITE, PauseMenuScreen::invitePlayer, Supplier::get));
        inviteButton.active = inviteButton.visible = false;
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics graphics, int ignored0, int ignored1, float ignored2, CallbackInfo ci) {
        inviteButton.active = inviteButton.visible = ClientConnectionHandler.getInstance()
            .isConnected() && E4MCStore.hasLink();
    }

    private static class IconButton extends Button {
        private final ResourceLocation icon;
        private int iconWidth;
        private int iconHeight;

        @SuppressWarnings("SuspiciousNameCombination")
        public IconButton(int x, int y, ResourceLocation icon, OnPress onPress, CreateNarration createNarration) {
            super(x, y, Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT, Component.empty(), onPress, createNarration);
            this.icon = icon;
            this.recomputeSize();
        }

        private void recomputeSize() {
            var texture = Minecraft.getInstance().getTextureManager().getTexture(icon).getTexture();
            var mipLevels = texture.getMipLevels();
            iconWidth = texture.getWidth(mipLevels);
            iconHeight = texture.getHeight(mipLevels);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int p_282682_, int p_281714_, float p_282542_) {
            super.renderWidget(guiGraphics, p_282682_, p_281714_, p_282542_);

            int scale = Minecraft.getInstance().options.guiScale().get();
            int displayWidth = iconWidth * scale;
            int displayHeight = iconHeight * scale;
            int offsetX = (width - displayWidth) / 2;
            int offsetY = (height - displayHeight) / 2;
            guiGraphics.blit(RenderType::guiTextured, icon, getX() + offsetX + 1, getY() + offsetY + 1, 0, 0,
                displayWidth, displayHeight, iconWidth, iconHeight, iconWidth, iconHeight, Colors.BLACK.get());
            guiGraphics.blit(RenderType::guiTextured, icon, getX() + offsetX, getY() + offsetY, 0, 0, displayWidth,
                displayHeight, iconWidth, iconHeight, iconWidth, iconHeight, Colors.WHITE.get());
        }
    }
}
