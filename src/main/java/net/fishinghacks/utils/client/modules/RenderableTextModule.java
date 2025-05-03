package net.fishinghacks.utils.client.modules;

import net.fishinghacks.utils.common.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import java.util.List;

public abstract class RenderableTextModule extends RenderableModule {
    public abstract List<Component> getText();
    public abstract List<Component> getPreviewText();
    public @Nullable Integer getBackgroundColor() { return null; }

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
        Integer bg = getBackgroundColor();
        if(bg != null) guiGraphics.fill(pos.x, pos.y, pos.x + width, pos.y + height, bg);
        boolean rightAlign = isRightAligned();
        for(int i = 0; i < text.size(); ++i) {
            int x = pos.x + 2;
            if(rightAlign)
                x += (width - font.width(text.get(i)) - 4);
            guiGraphics.drawString(font, text.get(i), x, pos.y + 1 + font.lineHeight * i, Colors.WHITE.get());
        }
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, float partialTick) {
        List<Component> text = getPreviewText();
        Font font = Minecraft.getInstance().font;
        int width = text.stream().map(font::width).reduce(0, Math::max) + 4;
        int height = font.lineHeight * text.size() + 2;
        Vector2i pos = getPosition(width, height);
        Integer bg = getBackgroundColor();
        if(bg != null) guiGraphics.fill(pos.x, pos.y, pos.x + width, pos.y + height, bg);
        boolean rightAlign = isRightAligned();
        for(int i = 0; i < text.size(); ++i) {
            int x = pos.x + 2;
            if(rightAlign)
                x += (width - font.width(text.get(i)) - 4);
            guiGraphics.drawString(font, text.get(i), x, pos.y + 1 + font.lineHeight * i, Colors.WHITE.get());
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
