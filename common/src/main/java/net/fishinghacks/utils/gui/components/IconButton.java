package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class IconButton extends Button {
    public static final int DEFAULT_WIDTH = Button.CUBE_WIDTH;

    private final ResourceLocation icon;
    private int iconWidth;
    private int iconHeight;
    public int color = Colors.WHITE.get();

    protected IconButton(int x, int y, ResourceLocation icon, @Nullable Consumer<Button> onPress) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, Component.empty(), onPress);

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
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        int scale = Minecraft.getInstance().options.guiScale().get();
        int displayWidth = iconWidth * scale;
        int displayHeight = iconHeight * scale;
        int offsetX = (width - displayWidth) / 2;
        int offsetY = (height - displayHeight) / 2;
        guiGraphics.blit(RenderType::guiTextured, icon, getX() + offsetX + 1, getY() + offsetY + 1, 0, 0, displayWidth,
            displayHeight, iconWidth, iconHeight, iconWidth, iconHeight, Colors.BLACK.get());
        guiGraphics.blit(RenderType::guiTextured, icon, getX() + offsetX, getY() + offsetY, 0, 0, displayWidth,
            displayHeight, iconWidth, iconHeight, iconWidth, iconHeight, color);
    }

    public static class Builder {
        protected int x = 0;
        protected int y = 0;
        protected int color = Colors.WHITE.get();
        protected boolean active = true;
        protected ResourceLocation icon;
        protected @Nullable Consumer<Button> onPress = null;

        public Builder(ResourceLocation icon) {
            this.icon = icon;
        }

        public IconButton build() {
            IconButton b = new IconButton(x, y, icon, onPress);
            b.active = active;
            b.color = color;
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

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder onPress(Consumer<Button> onPress) {
            this.onPress = onPress;
            return this;
        }
    }
}
