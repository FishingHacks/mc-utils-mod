package net.fishinghacks.utils.gui.cosmetics;

import net.fishinghacks.utils.gui.components.Box;
import net.fishinghacks.utils.gui.components.DummyAbstractWidget;
import net.fishinghacks.utils.Colors;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class CosmeticsBox extends Box {
    private boolean isSelected;
    private final Consumer<Boolean> onPress;

    public CosmeticsBox(Consumer<Boolean> onPress, int x, int y, int width, int height) {
        super(new DummyAbstractWidget());
        this.setRectangle(width, height, x, y);
        this.onPress = onPress;
        this.active = true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getRight() && mouseY < this.getBottom();
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.borderColor = isSelected ? Colors.SECONDARY_DARK.get() : DEFAULT_BORDER_COLOR;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onPress.accept(!isSelected);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(@NotNull FocusNavigationEvent ignored) {
        if (!this.active || !this.visible) {
            return null;
        } else {
            return !this.isFocused() ? ComponentPath.leaf(this) : null;
        }
    }

    @Nullable
    public ComponentPath getCurrentFocusPath() {
        return this.isFocused() ? ComponentPath.leaf(this) : null;
    }
}
