package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class Button extends AbstractButton {
    public static final int SMALL_WIDTH = 90;
    public static final int DEFAULT_WIDTH = 120;
    public static final int BIG_WIDTH = 150;
    public static final int CUBE_WIDTH = 16;
    public static final int DEFAULT_HEIGHT = 16;
    @Nullable protected Consumer<Button> onPress;

    protected Button(int x, int y, int width, int height, Component message, @Nullable Consumer<Button> onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    public void onPress(@Nullable Consumer<Button> onPress) {
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color = Colors.DARK.get();
        if (isFocused() || isHovered())
            color = Colors.DARK_HIGHLIGHT.get();
        if (!isActive())
            color = Colors.DARK_DISABLED.get();
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK.get());
        guiGraphics.fill(getX(), getY(), getX() + getWidth() - 1, getY() + getHeight() - 1, color);
        renderString(guiGraphics, Minecraft.getInstance().font, Colors.WHITE.get());
    }

    public void render(RenderType typ, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(!visible) return;
        isHovered = guiGraphics.containsPointInScissor(mouseX, mouseY) && mouseX >= getX() && mouseX < getRight() && mouseY >= getY() && mouseY < getBottom();

        int color = Colors.DARK.get();
        if (isFocused() || isHovered())
            color = Colors.DARK_HIGHLIGHT.get();
        if (!isActive())
            color = Colors.DARK_DISABLED.get();
        guiGraphics.fill(typ, getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK.get());
        guiGraphics.fill(typ, getX(), getY(), getX() + getWidth() - 1, getY() + getHeight() - 1, color);
        renderString(guiGraphics, Minecraft.getInstance().font, Colors.WHITE.get());
    }

    @Override
    public void onPress() {
        if(onPress != null) this.onPress.accept(this);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public static class Builder {
        protected int x = 0;
        protected int y = 0;
        protected int width;
        protected int height;
        protected boolean active = true;
        protected Component message;
        protected @Nullable Consumer<Button> onPress = null;

        protected Builder(int width, int height, Component message) {
            this.width = width;
            this.height = height;
            this.message = message;
        }
        public Builder(Component message) {
            this(DEFAULT_WIDTH, DEFAULT_HEIGHT, message);
        }

        public static Builder big(Component message) {
            return new Builder(BIG_WIDTH, DEFAULT_HEIGHT, message);
        }
        public static Builder normal(Component message) {
            return new Builder(message);
        }
        public static Builder small(Component message) {
            return new Builder(SMALL_WIDTH, DEFAULT_HEIGHT, message);
        }
        public static Builder cube(Component message) {
            return new Builder(CUBE_WIDTH, DEFAULT_HEIGHT, message);
        }
        public static Builder cube(String message) {
            return new Builder(CUBE_WIDTH, DEFAULT_HEIGHT, Component.literal(message));
        }
        public static Builder cube(char message) {
            return new Builder(CUBE_WIDTH, DEFAULT_HEIGHT, Component.literal(message+""));
        }

        public Button build() {
            Button b = new Button(x, y, width, height, message, onPress);
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

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder message(Component message) {
            this.message = message;
            return this;
        }

        public Builder onPress(Consumer<Button> onPress) {
            this.onPress = onPress;
            return this;
        }
    }
}
