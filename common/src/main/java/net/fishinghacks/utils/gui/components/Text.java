package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Colors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public class Text extends UnfocusableWidget {
    final float scale;
    private final Font font;
    private int color = Colors.WHITE.get();
    private int offsetX = 0;
    private int offsetY = 0;
    private boolean leftAlign = true;
    private boolean topAlign = true;

    public Text(int x, int y, int width, int height, Component message, Font font, float scale) {
        super(x, y, width, height, message);
        this.scale = scale;
        this.font = font;
        recomputeOffset();
    }

    public Text(int width, int height, Component message, Font font, float scale) {
        this(0, 0, width, height, message, font, scale);
    }

    public Text(int width, int height, Component message, float scale) {
        this(0, 0, width, height, message, Minecraft.getInstance().font, scale);
    }

    public Text(int width, int height, Component message) {
        this(0, 0, width, height, message, Minecraft.getInstance().font, 1f);
    }

    public Text(Component message, Font font, float scale) {
        this(0, 0, font.width(message), font.lineHeight, message, Minecraft.getInstance().font, scale);
    }

    public Text(Component message, Font font) {
        this(message, font, 1f);
    }

    public Text(Component message) {
        this(message, Minecraft.getInstance().font, 1f);
    }

    public Text(Component message, float scale) {
        this(message, Minecraft.getInstance().font, scale);
    }

    public void setLeftAlign(boolean leftAlign) {
        this.leftAlign = leftAlign;
    }

    public void setTopAlign(boolean topAlign) {
        this.topAlign = topAlign;
    }

    @Override
    public void setMessage(@NotNull Component message) {
        super.setMessage(message);
        recomputeOffset();
    }

    private void recomputeOffset() {
        if (leftAlign && topAlign) {
            offsetX = 0;
            offsetY = 0;
            return;
        }
        int actualWidth = (int) ((float) width / scale);
        int actualHeight = (int) ((float) height / scale);
        offsetX = 0;
        offsetY = 0;
        int textWidth = font.width(getMessage());
        if (textWidth < actualWidth && !leftAlign) offsetX = (actualWidth - textWidth) / 2;
        if (!topAlign)
            offsetY = (actualHeight - Math.min(font.wordWrapHeight(getMessage(), actualWidth), actualHeight)) / 2;
    }

    public void setColor(int color) {
        this.color = color;
    }

    protected final Font getFont() {
        return this.font;
    }

    protected final int getColor() {
        return this.color;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput ignored) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        guiGraphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) getX(), (float) getY(), 0);
        guiGraphics.pose().scale(this.scale, this.scale, this.scale);
        guiGraphics.drawWordWrap(getFont(), getMessage(), offsetX, offsetY, (int) ((float) width / scale) - offsetX,
            Colors.WHITE.get());
        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
    }

    public static class Builder {
        int x = 0;
        int y = 0;
        int width;
        int height;
        final Font font;
        MutableComponent message;
        boolean customWidth = true;
        boolean customHeight = true;
        float scale = 1.0f;
        int color = Colors.WHITE.get();

        public Builder(int width, int height, MutableComponent message, Font font) {
            this.font = font;
            this.width = width;
            this.height = height;
            this.message = message;
        }

        public Builder(int width, int height, MutableComponent message) {
            this(width, height, message, Minecraft.getInstance().font);
        }

        public Builder(MutableComponent message, Font font) {
            this(font.width(message), font.lineHeight, message, font);
            customWidth = false;
            customHeight = false;
        }

        public Builder(MutableComponent message) {
            this(message, Minecraft.getInstance().font);
        }

        public Builder(int width, int height, String message, Font font) {
            this(width, height, Component.literal(message), font);
        }

        public Builder(int width, int height, String message) {
            this(width, height, Component.literal(message));
        }

        public Builder(String message, Font font) {
            this(Component.literal(message), font);
        }

        public Builder(String message) {
            this(Component.literal(message));
        }

        public Builder width(int width) {
            this.width = width;
            customWidth = true;
            this.resize();
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            customHeight = true;
            this.resize();
            return this;
        }

        public Builder bold() {
            message = message.withStyle(ChatFormatting.BOLD);
            this.resize();
            return this;
        }

        public Builder italic() {
            message = message.withStyle(ChatFormatting.ITALIC);
            this.resize();
            return this;
        }

        public Builder underline() {
            message = message.withStyle(ChatFormatting.UNDERLINE);
            this.resize();
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            this.resize();
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

        public Text build() {
            return new Text(x, y, width, height, message, font, scale);
        }

        private void resize() {
            if (customWidth && customHeight) return;
            if (customWidth) {
                height = (int) ((float) font.wordWrapHeight(message, (int) ((float) width / scale)) * scale);
                return;
            }
            width = (int) ((float) font.width(message) * scale);
        }
    }
}
