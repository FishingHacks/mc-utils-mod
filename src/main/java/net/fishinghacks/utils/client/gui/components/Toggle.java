package net.fishinghacks.utils.client.gui.components;

import net.fishinghacks.utils.common.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class Toggle extends AbstractWidget {
    public static final int DEFAULT_HEIGHT = 12;
    public static final int DEFAULT_WIDTH = 24;
    private boolean checked;
    @Nullable OnChange onChange;

    protected Toggle(int x, int y, int width, int height, Component message, @Nullable OnChange onChange, boolean checked) {
        super(x, y, width, height, message);
        this.checked = checked;
        this.onChange = onChange;
    }

    public void onChange(@Nullable OnChange onChange) {
        this.onChange = onChange;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        this.setChecked(!checked);
        if(onChange != null) onChange.onChange(this, this.checked);
        super.onClick(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible) {
            if (CommonInputs.selected(keyCode)) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.setChecked(!this.checked);
                if(onChange != null) onChange.onChange(this, this.checked);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        int halfWidth = width / 2;

        guiGraphics.fill(x, y, x + width, y + height, Colors.BLACK.get());
        if(checked) {
            int color = Colors.SECONDARY.get();
            if (isFocused() || isHovered())
                color = Colors.SECONDARY_LIGHT.get();
            if (!isActive())
                color = Colors.SECONDARY_DARK.get();
            guiGraphics.fill(x, y, x + width - 1, y + height - 1, color);
            guiGraphics.fill(x + 1, y + 1, x + halfWidth, y + height - 2, Colors.BLACK.get());
            guiGraphics.fill(x + 2, y + 2, x + halfWidth, y + height - 2, Colors.DARK.get());
        } else {
            int color = Colors.GRAY.get();
            if (isFocused() || isHovered())
                color = Colors.LIGHT_GRAY.get();
            if (!isActive())
                color = Colors.DARK_DISABLED.get();

            guiGraphics.fill(x, y, x + width - 1, y + height - 1, color);
            guiGraphics.fill(x + halfWidth, y + 1, x + width - 2, y + height - 2, Colors.BLACK.get());
            guiGraphics.fill(x + halfWidth + 1, y + 2, x + width - 2, y + height - 2, Colors.DARK.get());
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @FunctionalInterface
    public interface OnChange {
        void onChange(Toggle toggle, boolean value);
    }

    public static class Builder {
        protected int x = 0;
        protected int y = 0;
        protected boolean active = true;
        protected boolean checked = false;
        protected Component message;
        @Nullable protected OnChange onChange = null;

        public Builder(Component message) {
            this.message = message;
        }

        public Builder() {
            this(Component.empty());
        }

        public Toggle build() {
            Toggle t = new Toggle(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, message, onChange, checked);
            t.active = active;
            return t;
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

        public Builder message(Component message) {
            this.message = message;
            return this;
        }

        public Builder checked(boolean checked) {
            this.checked = checked;
            return this;
        }

        public Builder onChange(@Nullable OnChange onChange) {
            this.onChange = onChange;
            return this;
        }
    }
}
