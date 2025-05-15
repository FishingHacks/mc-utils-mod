package net.fishinghacks.utils.client.gui.components;

import net.fishinghacks.utils.common.Colors;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Box extends UnfocusableWidget {
    public static final int DEFAULT_BORDER_SIZE = 2;
    public static final int DEFAULT_BORDER_COLOR = Colors.DARK.get();
    public static final int DEFAULT_BACKGROUND_COLOR = Colors.BG_DARK.get();

    private final LayoutElement child;
    public int borderColor;
    public int backgroundColor;
    private Borders borders;
    int borderSize;

    public Box(LayoutElement el) {
        this(el, new Borders(), DEFAULT_BORDER_SIZE, DEFAULT_BORDER_COLOR, DEFAULT_BACKGROUND_COLOR);
    }

    public Box(LayoutElement el, Borders border) {
        this(el, border, DEFAULT_BORDER_SIZE, DEFAULT_BORDER_COLOR, DEFAULT_BACKGROUND_COLOR);
    }

    public Box(LayoutElement el, Borders border, int borderSize) {
        this(el, border, borderSize, DEFAULT_BORDER_COLOR, DEFAULT_BACKGROUND_COLOR);
    }

    public Box(LayoutElement el, Borders border, int borderColor, int backgroundColor) {
        this(el, border, DEFAULT_BORDER_SIZE, borderColor, backgroundColor);
    }

    public Box(LayoutElement el, Borders borders, int borderSize, int borderColor, int backgroundColor) {
        super(0, 0, 0, 0, Component.empty());
        this.child = el;
        this.borders = borders;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.backgroundColor = backgroundColor;
        this.active = false;
        this.rearrangeElements();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int startX = getX();
        int startY = getY();
        int endX = getRight();
        int endY = getBottom();
        if (borders.left) startX += borderSize;
        if (borders.right) endX -= borderSize;
        if (borders.top) startY += borderSize;
        if (borders.bottom) endY -= borderSize;
        if (borders.hasAny()) guiGraphics.fill(getX(), getY(), getRight(), getBottom(), borderColor);
        guiGraphics.fill(startX, startY, endX, endY, backgroundColor);
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
        rearrangeElements();
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Borders getBorders() {
        return borders;
    }

    public void setBorders(Borders borders) {
        this.borders = borders;
        int x = this.getX();
        int y = this.getY();
        if (borders.left) x -= borderSize;
        if (borders.top) y -= borderSize;
        this.child.setX(x);
        this.child.setY(y);
        this.rearrangeElements();
    }

    public boolean hasTopBorder() {
        return borders.top;
    }

    public boolean hasBottomBorder() {
        return borders.bottom;
    }

    public boolean hasLeftBorder() {
        return borders.left;
    }

    public boolean hasRightBorder() {
        return borders.right;
    }

    public void setTopBorder(boolean top) {
        setBorders(borders.setTop(top));
    }

    public void setBottomBorder(boolean bottom) {
        setBorders(borders.setBottom(bottom));
    }

    public void setLeftBorder(boolean left) {
        setBorders(borders.setLeft(left));
    }

    public void setRightBorder(boolean right) {
        setBorders(borders.setRight(right));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        if (borders.left) x += borderSize;
        child.setX(x);
        this.rearrangeElements();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        if (borders.top) y += borderSize;
        child.setY(y);
        this.rearrangeElements();
    }

    @Override
    public void setPosition(int x, int y) {
        super.setX(x);
        super.setY(y);
        child.setX(x);
        child.setY(y);
        this.rearrangeElements();
    }

    public void setWidth(int width) {
        if (child instanceof AbstractWidget w) {
            if (borders.right) width -= borderSize;
            if (borders.left) width -= borderSize;
            w.setWidth(width);
            this.rearrangeElements();
            super.setWidth(width);
        }
    }

    public void setHeight(int height) {
        if (child instanceof AbstractWidget w) {
            if (borders.top) height -= borderSize;
            if (borders.bottom) height -= borderSize;
            w.setHeight(height);
            this.rearrangeElements();
            super.setHeight(height);
        }
    }

    public void setSize(int width, int height) {
        if (child instanceof AbstractWidget w) {
            if (borders.top) height -= borderSize;
            if (borders.bottom) height -= borderSize;
            if (borders.left) width -= borderSize;
            if (borders.right) width -= borderSize;
            w.setWidth(width);
            w.setHeight(height);
            this.rearrangeElements();
        }
    }

    @Override
    public int getWidth() {
        width = child.getWidth();
        if (borders.left) width += borderSize;
        if (borders.right) width += borderSize;
        return width;
    }

    @Override
    public int getHeight() {
        height = child.getHeight();
        if (borders.top) height += borderSize;
        if (borders.bottom) height += borderSize;
        return height;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
        child.visitWidgets(consumer);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput ignored) {
    }

    private void rearrangeElements() {
        if (this.child instanceof Layout l) l.arrangeElements();

        width = getWidth();
        height = getHeight();
    }

    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(getX(), getY(), width, height);
    }

    public record Borders(boolean top, boolean bottom, boolean left, boolean right) {

        public Borders() {
            this(true, true, true, true);
        }

        public static final Borders NONE = new Borders(false, false, false, false);
        public static final Borders HORIZONTAL = new Borders(false, false, true, true);
        public static final Borders VERTICAL = new Borders(true, true, false, false);

        private boolean hasAny() {
            return top || bottom || left || right;
        }

        public Borders setLeft(boolean left) {
            return new Borders(top, bottom, left, right);
        }

        public Borders setRight(boolean right) {
            return new Borders(top, bottom, left, right);
        }

        public Borders setTop(boolean top) {
            return new Borders(top, bottom, left, right);
        }

        public Borders setBottom(boolean bottom) {
            return new Borders(top, bottom, left, right);
        }

        public Borders merge(Borders other) {
            return new Borders(top || other.top, bottom || other.bottom, left || other.left, right || other.right);
        }
    }
}
