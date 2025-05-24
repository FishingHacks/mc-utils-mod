package net.fishinghacks.utils.gui.components;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ScrollableList extends AbstractWidget {
    int scrollOffset;
    private final LayoutElement child;

    public ScrollableList(int x, int y, int width, int height, LayoutElement child, int scrollOffset) {
        super(x, y, width, height, Component.empty());
        this.child = child;
        this.scrollOffset = scrollOffset;
        child.setY(y - scrollOffset);
        child.setX(x);
        if (child instanceof Layout l) l.arrangeElements();
        childUpdated();
    }

    @Override
    protected boolean isValidClickButton(int i0) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double i0, double i1, int i2, double i3, double i4) {
        return false;
    }

    public void childUpdated() {
        if (scrollOffset + height > child.getHeight()) scrollOffset = child.getHeight() - height;
        if (scrollOffset < 0) scrollOffset = 0;
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
        childUpdated();
        child.setY(getY() - scrollOffset);
        if (child instanceof Layout l) l.arrangeElements();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset -= (int) (scrollY * 10.0);
        childUpdated();
        child.setY(getY() - scrollOffset);
        if (child instanceof Layout l) l.arrangeElements();
        return true;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.enableScissor(getX(), getY(), getRight(), getBottom());
        child.visitWidgets(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        guiGraphics.disableScissor();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }

    public void getChildren(Consumer<EventHolder> consumer) {
        child.visitWidgets(widget -> consumer.accept(new EventHolder(widget, this)));
    }

    public ScreenRectangle getBounds() {
        return new ScreenRectangle(getX(), getY(), getWidth(), getHeight());
    }

    public static class EventHolder implements GuiEventListener, NarratableEntry {
        private final AbstractWidget child;
        private final ScrollableList list;

        private EventHolder(AbstractWidget child, ScrollableList bounds) {
            this.child = child;
            this.list = bounds;
        }

        @Override
        public void setFocused(boolean b) {
            child.setFocused(b);
        }

        @Override
        public boolean isFocused() {
            return child.isFocused();
        }

        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return child.narrationPriority();
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
            child.updateNarration(narrationElementOutput);
        }

        @Override
        public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
            return child.active && child.visible && !child.isFocused() ? ComponentPath.leaf(this) : null;
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!list.getBounds().containsPoint((int) mouseX, (int) mouseY)) return false;
            return child.mouseClicked(mouseX, mouseY, button);
        }

        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (!list.getBounds().containsPoint((int) mouseX, (int) mouseY)) return false;
            return child.mouseReleased(mouseX, mouseY, button);
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return child.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (!list.getBounds().containsPoint((int) mouseX, (int) mouseY)) return false;
            return child.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return child.keyPressed(keyCode, scanCode, modifiers);
        }

        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return child.keyReleased(keyCode, scanCode, modifiers);
        }

        public boolean charTyped(char codePoint, int modifiers) {
            return child.charTyped(codePoint, modifiers);
        }

        public @NotNull ScreenRectangle getRectangle() {
            return child.getRectangle();
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            return list.getBounds().containsPoint((int) mouseX, (int) mouseY) && child.isMouseOver(mouseX, mouseY);
        }

        @Override
        public @NotNull ScreenRectangle getBorderForArrowNavigation(@NotNull ScreenDirection direction) {
            return child.getBorderForArrowNavigation(direction);
        }
    }
}