package net.fishinghacks.utils.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DummyAbstractWidget extends UnfocusableWidget {
    public DummyAbstractWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        active = false;
        visible = false;
    }
    public DummyAbstractWidget() {
        this(0, 0, 0, 0);
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
    protected void renderWidget(@NotNull GuiGraphics i0, int i1, int i2, float i3) {
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput i0) {
    }
}
