package net.fishinghacks.utils.client.gui.components;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Center implements LayoutElement {
    private int width;
    private int height;
    private int x;
    private int y;
    private final LayoutElement child;

    public Center(int x, int y, int width, int height, LayoutElement child) {
        this.width = width;
        this.height = height;
        this.child = child;
        setX(x);
        setY(y);
    }

    public void setWidth(int width) {
        this.width = Math.max(width, child.getWidth());
        setX(x);
    }

    public void setHeight(int height) {
        this.height = Math.max(height, child.getHeight());
        setY(y);
    }

    @Override
    public void setX(int x) {
        this.x = x;
        int offsetX = Math.max(width - child.getWidth(), 0) / 2;
        child.setX(x + offsetX);
    }

    @Override
    public void setY(int y) {
        this.y = y;
        int offsetY = Math.max(height - child.getHeight(), 0) / 2;
        child.setY(y + offsetY);
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
        return Math.max(width, child.getWidth());
    }

    @Override
    public int getHeight() {
        return Math.max(height, child.getHeight());
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
        child.visitWidgets(consumer);
    }
}
