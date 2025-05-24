package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class VanillaIconTextButton extends Button {
    private final ResourceLocation icon;
    private int iconWidth;
    private int iconHeight;

    public VanillaIconTextButton(int x, int y, ResourceLocation icon, Component message, OnPress onPress,
                                 CreateNarration createNarration) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, message, onPress, createNarration);
        this.icon = icon;
        this.recomputeSize();
    }

    public VanillaIconTextButton(int x, int y, ResourceLocation icon, Component message, OnPress onPress) {
        this(x, y, icon, message, onPress, Supplier::get);
    }

    public VanillaIconTextButton(int x, int y, ResourceLocation icon, Component message) {
        this(x, y, icon, message, ignored -> {
        }, Supplier::get);
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
        int offset = (height - displayHeight) / 2;
        guiGraphics.blit(RenderType::guiTextured, icon, getRight() - DEFAULT_HEIGHT + offset, getY() + offset + 1, 0, 0,
            displayWidth, displayHeight, iconWidth, iconHeight, iconWidth, iconHeight, Colors.BLACK.get());
        guiGraphics.blit(RenderType::guiTextured, icon, getRight() - DEFAULT_HEIGHT + offset - 1, getY() + offset, 0, 0,
            displayWidth, displayHeight, iconWidth, iconHeight, iconWidth, iconHeight, Colors.WHITE.get());
    }

    @Override
    public void renderString(@NotNull GuiGraphics guiGraphics, Font font, int color) {
        int width = getWidth() - Button.DEFAULT_HEIGHT - 10;
        int messageWidth = font.width(getMessage());
        if (messageWidth < width)
            guiGraphics.drawString(font, getMessage(), getX() + 5, getY() + (getHeight() - font.lineHeight) / 2 + 1,
                color);
        else renderScrollingString(guiGraphics, font, getMessage(), getX() + 5, getY(), getX() + width, getBottom(),
            color);
    }
}