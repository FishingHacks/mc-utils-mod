package net.fishinghacks.utils.modules;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.values.ColorValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;

import java.util.List;

public abstract class RenderableTextModule extends RenderableModule {
    public abstract List<Component> getText();

    public abstract List<Component> getPreviewText();

    public Integer getBackgroundColor() {
        return Colors.DARK_GRAY.withAlpha(0x7f);
    }

    protected CachedValue<Boolean> textShadow;
    protected CachedValue<Boolean> background;
    protected ColorValue fgCol;

    @Override
    public void buildConfig(Config cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);
        textShadow = CachedValue.wrap(cfg, builder, "text_shadow", true);
        background = CachedValue.wrap(cfg, builder, "background", true);
        fgCol = ColorValue.wrap(cfg, builder, "fg_color", Colors.WHITE.toCol());
    }

    private boolean isRightAligned() {
        return x >= (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        List<Component> text = getText();
        Font font = Minecraft.getInstance().font;
        int width = text.stream().map(font::width).reduce(0, Math::max) + 4;
        int height = font.lineHeight * text.size() + 2;
        Vector2i pos = getPosition(width, height);
        if (background.get()) guiGraphics.fill(pos.x, pos.y, pos.x + width, pos.y + height, getBackgroundColor());
        boolean rightAlign = isRightAligned();
        for (int i = 0; i < text.size(); ++i) {
            int x = pos.x + 2;
            if (rightAlign) x += (width - font.width(text.get(i)) - 4);
            guiGraphics.drawString(font, text.get(i), x, pos.y + 1 + font.lineHeight * i, fgCol.get().argb(),
                textShadow.get());
        }
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, float partialTick) {
        List<Component> text = getPreviewText();
        Font font = Minecraft.getInstance().font;
        int width = text.stream().map(font::width).reduce(0, Math::max) + 4;
        int height = font.lineHeight * text.size() + 2;
        Vector2i pos = getPosition(width, height);
        boolean rightAlign = isRightAligned();
        for (int i = 0; i < text.size(); ++i) {
            int x = pos.x + 2;
            if (rightAlign) x += (width - font.width(text.get(i)) - 4);
            guiGraphics.drawString(font, text.get(i), x, pos.y + 1 + font.lineHeight * i, fgCol.get().argb(),
                textShadow.get());
        }
    }

    @Override
    public Vector2i previewSize() {
        List<Component> text = getPreviewText();
        Font font = Minecraft.getInstance().font;
        int width = text.stream().map(font::width).reduce(0, Math::max) + 4;
        int height = font.lineHeight * text.size() + 2;
        return new Vector2i(width, height);
    }
}
