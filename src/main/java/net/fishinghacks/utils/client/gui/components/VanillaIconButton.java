package net.fishinghacks.utils.client.gui.components;

import net.fishinghacks.utils.common.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class VanillaIconButton extends Button {
        private final ResourceLocation icon;
        private int iconWidth;
        private int iconHeight;
        public static int DEFAULT_HEIGHT = Button.DEFAULT_HEIGHT;
        @SuppressWarnings("SuspiciousNameCombination")
        public static int DEFAULT_WIDTH = Button.DEFAULT_HEIGHT;

        @SuppressWarnings("SuspiciousNameCombination")
        public VanillaIconButton(int x, int y, ResourceLocation icon, OnPress onPress, CreateNarration createNarration) {
            super(x, y, DEFAULT_HEIGHT, DEFAULT_WIDTH, Component.empty(), onPress, createNarration);
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
