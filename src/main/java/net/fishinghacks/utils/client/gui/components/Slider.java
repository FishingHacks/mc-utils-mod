package net.fishinghacks.utils.client.gui.components;

import net.fishinghacks.utils.common.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.time.Duration;

public class Slider extends AbstractWidget {
    public static final int DEFAULT_HEIGHT = 16;
    public static final int DEFAULT_WIDTH = 150;
    public static final int barWidth = 1;

    private int minValue;
    private int maxValue;
    private int value;
    private int steps;
    private @Nullable OnChange onChange;
    private boolean isSelected = false;
    private boolean inputReceived = false;

    public Slider(int minValue, int maxValue, int value, @Nullable Component message) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, minValue, maxValue, value, message);
    }

    public Slider(int x, int y, int minValue, int maxValue, int value, @Nullable Component message) {
        this(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, minValue, maxValue, value, message);
    }

    public Slider(int minValue, int maxValue, int value) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, minValue, maxValue, value, Component.empty());
    }

    public Slider(int x, int y, int minValue, int maxValue, int value) {
        this(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, minValue, maxValue, value, Component.empty());
    }

    public Slider(int x, int y, int width, int height, int minValue, int maxValue, int value,
                  @Nullable Component message) {
        super(x, y, width, height, message == null ? Component.empty() : message);
        if (minValue >= maxValue || value < minValue || value > maxValue) throw new IllegalStateException();
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = value;
        steps = maxValue - minValue + 1;
        super.setTooltipDelay(Duration.ZERO);
        super.setTooltip(Tooltip.create(message == null ? Component.literal("" + value) : message));
    }

    @Override
    public void setTooltipDelay(@NotNull Duration ignored) {
    }

    @Override
    public void setTooltip(@Nullable Tooltip ignored) {
    }

    @Override
    public void setMessage(@NotNull Component message) {
        super.setMessage(message);
        super.setTooltip(Tooltip.create(message));
    }

    public void onChange(OnChange onChange) {
        this.onChange = onChange;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        if (minValue >= maxValue) throw new IllegalStateException();
        this.maxValue = maxValue;
        this.steps = maxValue - minValue + 1;
    }

    public void setMinValue(int minValue) {
        if (minValue >= maxValue) throw new IllegalStateException();
        this.minValue = minValue;
        this.steps = maxValue - minValue + 1;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        value = Math.clamp(value, minValue, maxValue);
        if (this.value == value) return;
        this.value = value;
        super.setTooltip(Tooltip.create(Component.literal("" + value)));
        if (this.onChange != null) this.onChange.onChange(this, this.value);
    }

    private void setValueFromMouse(int mouseX) {
        mouseX -= getX();
        if (mouseX < 0 || mouseX >= getWidth()) return;
        int val = minValue + Math.clamp((long) mouseX * (long) steps / (long) (width - 1), 0, maxValue - minValue);
        setValue(val);
        isSelected = true;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return;
        this.setValueFromMouse((int) mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double ignored0, double ignored1) {
        if (!isMouseOver(mouseX, mouseY)) return;
        this.setValueFromMouse((int) mouseX);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (CommonInputs.selected(keyCode)) {
            isSelected = !isSelected;
            inputReceived = false;
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        if (!isSelected) return super.keyPressed(keyCode, scanCode, modifiers);
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE:
                isSelected = false;
                inputReceived = false;
                return false;
            case GLFW.GLFW_KEY_DELETE:
            case GLFW.GLFW_KEY_BACKSPACE:
                inputReceived = false;
                return false;
            case GLFW.GLFW_KEY_LEFT:
                setValue(value - 1);
                return true;
            case GLFW.GLFW_KEY_RIGHT:
                setValue(value + 1);
                return true;
            case GLFW.GLFW_KEY_MINUS:
                setValue(-value);
                return true;
            case GLFW.GLFW_KEY_0:
            case GLFW.GLFW_KEY_1:
            case GLFW.GLFW_KEY_2:
            case GLFW.GLFW_KEY_6:
            case GLFW.GLFW_KEY_3:
            case GLFW.GLFW_KEY_4:
            case GLFW.GLFW_KEY_5:
            case GLFW.GLFW_KEY_7:
            case GLFW.GLFW_KEY_8:
            case GLFW.GLFW_KEY_9:
                break;
            default:
                return true;
        }
        int value = keyCode - GLFW.GLFW_KEY_0;
        if (inputReceived) value += this.value * 10;
        inputReceived = true;
        setValue(value);

        return true;
    }

    @Override
    public void setFocused(boolean focused) {
        if (!focused) isSelected = false;
        super.setFocused(focused);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color = Colors.PRIMARY.get();
        int darkColor = Colors.DARK.get();
        if(!active) {
            darkColor = color = Colors.DARK_DISABLED.get();
        }
        if (isHoveredOrFocused()) {
            color = Colors.SECONDARY_DARK.get();
            darkColor = Colors.DARK_HIGHLIGHT.get();
        }
        if(isSelected) {
            color = Colors.SECONDARY_LIGHT.get();
            darkColor = Colors.DARK_SELECTED.get();
        }

        guiGraphics.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK.get());
        guiGraphics.fill(getX(), getY(), getRight() - 1, getBottom() - 1, darkColor);

        float halfStepWidth = (float) (width - barWidth - 1) / (float) (steps);
        float valueRelZero = (float) (value - minValue);
        float offsetX = valueRelZero * halfStepWidth;
        offsetX += Mth.lerp(valueRelZero / (float) (maxValue - minValue), 0f, halfStepWidth);
        int offset = Math.clamp((int) offsetX, 0, width);

        guiGraphics.fill(getX(), getY(), getX() + offset + barWidth, getBottom() - 1, color);
        guiGraphics.fill(getX() + offset, getY(), getX() + offset + barWidth, getBottom() - 1,
            Colors.WHITE.get());
        renderScrollingString(guiGraphics, Minecraft.getInstance().font, getMessage(), getX(), getY(), getRight(),
            getBottom(), Colors.WHITE.get());
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    @FunctionalInterface
    public interface OnChange {
        void onChange(Slider slider, int value);
    }
}
