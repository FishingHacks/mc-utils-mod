package net.fishinghacks.utils.gui.components;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Spacer implements LayoutElement {
    private int x;
    private int y;
    private int width;
    private int height;
    private final int minWidth;
    private final int minHeight;

    public Spacer(int x, int y, int minWidth, int minHeight) {
        this.x = x;
        this.y = y;
        this.width = this.minWidth = minWidth;
        this.height = this.minHeight = minHeight;
    }

    public Spacer(int width, int height) {
        this(0, 0, width, height);
    }

    public void setWidth(int width) {
        this.width = Math.max(width, minWidth);
    }

    public void setHeight(int height) {
        this.height = Math.max(height, minHeight);
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> ignored) {
    }
}
