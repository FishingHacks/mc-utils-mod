package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class IconTextButton extends Button {
    private ResourceLocation icon;
    private int iconWidth;
    private int iconHeight;

    protected IconTextButton(int x, int y, ResourceLocation icon, Component message, @Nullable Consumer<Button> onPress,
                             int width) {
        super(x, y, width, DEFAULT_HEIGHT, message, onPress);

        this.icon = icon;
        this.recomputeSize();
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public void setIcon(ResourceLocation icon) {
        this.icon = icon;
        recomputeSize();
    }

    private void recomputeSize() {
        var texture = Minecraft.getInstance().getTextureManager().getTexture(icon).getTexture();
        var mipLevels = texture.getMipLevels();
        iconWidth = texture.getWidth(mipLevels);
        iconHeight = texture.getHeight(mipLevels);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        int scale = Minecraft.getInstance().options.guiScale().get();
        int displayWidth = iconWidth * scale;
        int displayHeight = iconHeight * scale;
        int offset = (height - displayHeight) / 2;
        guiGraphics.blit(RenderType::guiTextured, icon, getRight() - CUBE_WIDTH + offset, getY() + offset + 1, 0, 0,
            displayWidth, displayHeight, iconWidth, iconHeight, iconWidth, iconHeight, Colors.BLACK.get());
        guiGraphics.blit(RenderType::guiTextured, icon, getRight() - CUBE_WIDTH + offset - 1, getY() + offset, 0, 0,
            displayWidth, displayHeight, iconWidth, iconHeight, iconWidth, iconHeight, Colors.WHITE.get());
    }

    @Override
    public void renderString(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int color) {
        int width = getWidth() - CUBE_WIDTH - 6;
        int messageWidth = font.width(getMessage());
        if (messageWidth < width)
            guiGraphics.drawString(font, getMessage(), getX() + 3, getY() + (getHeight() - font.lineHeight) / 2 + 1,
                color);
        else renderScrollingString(guiGraphics, font, getMessage(), getX(), getY(), getX() + width, getBottom(), color);
    }

    public static class Builder {
        protected int x = 0;
        protected int y = 0;
        protected boolean active = true;
        protected ResourceLocation icon;
        protected Component message;
        protected @Nullable Consumer<Button> onPress = null;
        protected int width = IconTextButton.DEFAULT_WIDTH;

        public Builder(ResourceLocation icon, Component message) {
            this.icon = icon;
            this.message = message;
        }

        public Builder(ResourceLocation icon, Component message, int width) {
            this.icon = icon;
            this.message = message;
            this.width = width;
        }

        public static Builder small(ResourceLocation icon, Component message) {
            return new Builder(icon, message, SMALL_WIDTH);
        }

        public static Builder normal(ResourceLocation icon, Component message) {
            return new Builder(icon, message);
        }

        public static Builder big(ResourceLocation icon, Component message) {
            return new Builder(icon, message, BIG_WIDTH);
        }

        public IconTextButton build() {
            IconTextButton b = new IconTextButton(x, y, icon, message, onPress, width);
            b.active = active;
            return b;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder onPress(Consumer<Button> onPress) {
            this.onPress = onPress;
            return this;
        }
    }
}
