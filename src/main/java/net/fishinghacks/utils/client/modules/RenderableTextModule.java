package net.fishinghacks.utils.client.modules;

import net.fishinghacks.utils.common.Colors;
import net.fishinghacks.utils.common.config.CachedValue;
import net.fishinghacks.utils.common.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import java.util.List;

public abstract class RenderableTextModule extends RenderableModule {
    public abstract List<Component> getText();

    public abstract List<Component> getPreviewText();

    public Integer getBackgroundColor() {
        return Colors.DARK_GRAY.withAlpha(0x7f);
    }

    protected CachedValue<Boolean> textShadow;
    protected CachedValue<Boolean> background;

    @Override
    public void buildConfig(Config cfg, ModConfigSpec.Builder builder) {
        super.buildConfig(cfg, builder);
        textShadow = CachedValue.wrap(cfg, builder.define("text_shadow", true));
        background = CachedValue.wrap(cfg, builder.define("background", true));
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
            guiGraphics.drawString(font, text.get(i), x, pos.y + 1 + font.lineHeight * i, Colors.WHITE.get(),
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
            guiGraphics.drawString(font, text.get(i), x, pos.y + 1 + font.lineHeight * i, Colors.WHITE.get(),
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
