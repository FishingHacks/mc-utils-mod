package net.fishinghacks.utils.client.gui.cosmetics;

import net.fishinghacks.utils.client.gui.components.Box;
import net.fishinghacks.utils.client.gui.components.Button;
import net.fishinghacks.utils.client.gui.components.DummyAbstractWidget;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CosmeticsBox extends Box {
    public final Button button;

    public CosmeticsBox(Consumer<Button> onPress, int x, int y, int width, int height) {
        super(new DummyAbstractWidget());
        this.setRectangle(width, height, x, y);
        this.button = Button.Builder.cube("✔").onPress(onPress).build();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return getRectangle().containsPoint((int) mouseX, (int) mouseY);
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.button.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        button.setPosition(getRight() - Button.CUBE_WIDTH - 3, getBottom() - Button.DEFAULT_HEIGHT - 3);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        button.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isFocused() {
        return button.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        button.setFocused(focused);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent ev) {
        return !this.isFocused() ? ComponentPath.leaf(this) : null;
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        return this.isFocused() ? ComponentPath.leaf(this) : null;
    }
}
