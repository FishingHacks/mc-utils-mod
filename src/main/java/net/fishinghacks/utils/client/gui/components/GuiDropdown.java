package net.fishinghacks.utils.client.gui.components;

import net.fishinghacks.utils.common.Colors;
import net.fishinghacks.utils.client.gui.GuiOverlayManager;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuiDropdown<T> extends AbstractWidget {
    public static final String DROPDOWN_ICON = "⏷";
    public static final String DROPDOWN_ICON_OPEN = "⏶";
    public static final String DROPDOWN_ICON_SELECTED = "✔ ";
    public static final int DEFAULT_WIDTH = 120;
    public static final int DEFAULT_HEIGHT = 16;

    private boolean expanded;
    private T value;
    private Component message;
    private final List<T> validValues;
    private final List<Component> validValueStrings;
    private final Function<T, Component> valueStringifier;
    @Nullable
    private OnValueChange<T> onValueChange;
    private int selectedIdx = 0;

    // Fixes a problem where setFocused(false) and setFocused(true) are called in rapid succession, when trying to
    // open the dropdown while its already focused.
    // This is caused due to `AbstractContainerEventHandler` (from Screen) not handling it correctly, when the focus
    // switches to the already focused element.
    // `AbstractContainerEventHandler::setFocused` removes focus from the currently focused element and then sets
    // focus for the new element, without checking if they're the same or not.
    private long lastFocusUnExpand;

    public GuiDropdown(int x, int y, int width, int height, Component message, T value,
                       Function<T, Component> valueStringifier, @Nullable OnValueChange<T> onValueChange,
                       List<T> validValues) {
        super(x, y, width, height, message);
        this.value = value;
        this.valueStringifier = valueStringifier;
        this.onValueChange = onValueChange;
        this.validValues = validValues;
        this.validValueStrings = validValues.stream().map(valueStringifier).toList();
        this.message = valueStringifier.apply(value);
    }

    public GuiDropdown(int x, int y, Component message, T value, Function<T, Component> valueStringifier,
                       @Nullable OnValueChange<T> onValueChange, List<T> validValues) {
        this(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, message, value, valueStringifier, onValueChange, validValues);
    }

    public GuiDropdown(Component message, T value, Function<T, Component> valueStringifier,
                       @Nullable OnValueChange<T> onValueChange, List<T> validValues) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, message, value, valueStringifier, onValueChange, validValues);
    }

    public GuiDropdown(T value, Function<T, Component> valueStringifier, @Nullable OnValueChange<T> onValueChange,
                       List<T> validValues) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, Component.empty(), value, valueStringifier, onValueChange,
            validValues);
    }

    public GuiDropdown(T value, Function<T, Component> valueStringifier, List<T> validValues) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, Component.empty(), value, valueStringifier, null, validValues);
    }

    public GuiDropdown(T value, Function<T, Component> valueStringifier, T[] validValues) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, Component.empty(), value, valueStringifier, null,
            Arrays.asList(validValues));
    }

    public static <T extends Enum<T>> GuiDropdown<T> fromEnum(T value, T[] values) {
        return new GuiDropdown<>(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, Component.empty(), value,
            v -> Component.literal(v.name()), (a, b) -> {
        }, Arrays.stream(values).toList());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (super.isMouseOver(mouseX, mouseY)) return true;
        if (!expanded) return false;
        if (!this.active || !this.visible) return false;
        if (mouseX < getX() || mouseX >= getRight() || mouseY < getY()) return false;
        int height = getHeight() + DEFAULT_HEIGHT * validValues.size();
        return mouseY < getY() + height;
    }

    public void setValue(T value) {
        this.value = value;
        if (onValueChange != null) onValueChange.onValueChange(this, value);
        setExpanded(false);
        message = valueStringifier.apply(value);
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        // hopefully no one will look at this tooltip for *80* years
        if (expanded) this.setTooltipDelay(Duration.ofSeconds(2524608000L));
        else this.setTooltipDelay(Duration.ZERO);
    }

    public T getValue() {
        return value;
    }

    public void onValueChange(@Nullable OnValueChange<T> listener) {
        this.onValueChange = listener;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (CommonInputs.selected(keyCode) && !expanded) {
            setExpanded(true);
            selectedIdx = 0;
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        if (!expanded) return super.keyPressed(keyCode, scanCode, modifiers);
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER:
                if (selectedIdx > 0 && selectedIdx <= validValues.size())
                    this.setValue(validValues.get(selectedIdx - 1));
                setExpanded(false);
                break;
            case GLFW.GLFW_KEY_DOWN:
                if (selectedIdx < validValues.size()) selectedIdx++;
                break;
            case GLFW.GLFW_KEY_UP:
                if (selectedIdx > 0) selectedIdx--;
                break;
        }

        return true;
    }

    @Override
    public void setFocused(boolean focused) {
        // bugfix for bug caused by minecraft. See `lastFocusUnexpand` for more info.
        if (focused && Util.getMillis() - lastFocusUnExpand < 50) setExpanded(true);
        if (!focused && expanded) {
            setExpanded(false);
            lastFocusUnExpand = Util.getMillis();
        }

        super.setFocused(focused);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (super.isMouseOver(mouseX, mouseY)) {
            setExpanded(!expanded);
            this.selectedIdx = 0;
            return;
        }
        int idx = ((int) mouseY - getY() - getHeight()) / DEFAULT_HEIGHT;
        if (idx < 0 || idx >= validValues.size()) return;
        this.setValue(validValues.get(idx));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color = Colors.DARK.get();
        if (isFocused() || isHovered()) color = Colors.DARK_HIGHLIGHT.get();
        if (!isActive()) color = Colors.DARK_DISABLED.get();

        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK.get());
        guiGraphics.fill(getX(), getY(), getX() + getWidth() - 1, getY() + getHeight() - 1, color);

        Font font = Minecraft.getInstance().font;
        String dropdownIcon = expanded ? DROPDOWN_ICON_OPEN : DROPDOWN_ICON;
        int dropdownIconWidth = 4 + font.width(dropdownIcon);
        int y = getY() + (height - font.lineHeight) / 2;
        guiGraphics.drawString(font, dropdownIcon, getX() + width - dropdownIconWidth, y + 1, Colors.WHITE.get());
        guiGraphics.drawScrollingString(Minecraft.getInstance().font, message, getX() + 3,
            getX() + width - dropdownIconWidth - 3, y, Colors.WHITE.get());
        if (!expanded) return;

        int dropdownHeight = DEFAULT_HEIGHT * validValues.size() + 2;
        GuiOverlayManager.setOverlay(this, getX(), getY() + getHeight() - 1, getWidth(), dropdownHeight + 1,
            this::renderOverlay);
    }

    protected void renderOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int dropdownY = getY() + height;
        int dropdownHeight = DEFAULT_HEIGHT * validValues.size() + 2;
        Font font = Minecraft.getInstance().font;

        RenderType overlay = RenderType.guiOverlay();
        guiGraphics.fill(overlay, getX(), getY() + getHeight() - 1, getX() + getWidth(),
            getY() + getHeight() + dropdownHeight + 1, Colors.BLACK.get());
        guiGraphics.fill(overlay, getX(), dropdownY - 1, getX() + width - 1, dropdownY + dropdownHeight,
            Colors.DARK_HIGHLIGHT.get());
        guiGraphics.fill(overlay, getX() + 2, dropdownY - 1, getX() + width - 3, dropdownY + dropdownHeight - 2,
            Colors.DARK.get());
        if (selectedIdx > 0 && selectedIdx <= validValues.size()) {
            int idx = selectedIdx - 1;
            int hoverY = dropdownY + idx * DEFAULT_HEIGHT;
            guiGraphics.fill(overlay, getX() + 2, hoverY, getX() + width - 3, hoverY + DEFAULT_HEIGHT,
                Colors.DARK_HOVER.get());
        }
        int idx = (mouseY - getY() - getHeight()) / DEFAULT_HEIGHT;
        if (idx >= 0 && idx < validValues.size() && mouseY >= getY() + getHeight() && mouseX >= getX() && mouseX < getRight()) {
            int hoverY = dropdownY + idx * DEFAULT_HEIGHT;
            guiGraphics.fill(overlay, getX() + 2, hoverY, getX() + width - 3, hoverY + DEFAULT_HEIGHT,
                Colors.DARK_HOVER.get());
        }
        int strYOff = (DEFAULT_HEIGHT - font.lineHeight) / 2;
        for (int i = 0; i < validValues.size(); ++i) {
            int strY = dropdownY + strYOff + i * DEFAULT_HEIGHT;
            Component comp = validValueStrings.get(i);
            if (validValues.get(i).equals(value)) {
                comp = Component.literal(DROPDOWN_ICON_SELECTED).append(comp);
            }
            guiGraphics.drawString(font, comp, getX() + 5, strY, Colors.WHITE.get());
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public interface OnValueChange<T> {
        void onValueChange(GuiDropdown<T> dropdown, T value);
    }
}
