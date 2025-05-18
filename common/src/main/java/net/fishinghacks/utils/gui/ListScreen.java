package net.fishinghacks.utils.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ListScreen extends BlackScreen {
    protected int listHeight;
    protected int listVisibleHeight;
    protected LinearLayout listLayout;
    protected int scrollHeight;
    protected int listStartY;
    protected int listStartX;
    protected int listWidth;
    protected AbstractWidget titleWidget;

    protected ListScreen(Component title, Screen parent) {
        super(title, parent);
    }

    protected abstract void onInit();

    protected abstract void buildList();

    protected int getListWidth() {
        return this.width / 4 * 3;
    }

    protected int getListStartX() {
        return (this.width - listWidth) / 2;
    }

    public int getListVisibleHeight() {
        return listVisibleHeight;
    }

    protected @Nullable AbstractWidget addTitle() {
        return new StringWidget(getTitle(), getFont());
    }

    public void scrollToHeight(int height, boolean clamp) {
        if (clamp) scrollHeight = Math.max(Math.min(height, listHeight + 10) - listVisibleHeight, 0);
        else scrollHeight = Math.max(height - listVisibleHeight, 0);
        listLayout.setY(listStartY - scrollHeight);
        listLayout.arrangeElements();
    }

    @Override
    protected final void init() {
        super.init();
        titleWidget = addTitle();
        listVisibleHeight = height - 30;
        listStartY = 30;
        listWidth = getListWidth();
        listStartX = getListStartX();
        if (titleWidget != null) {
            titleWidget.setPosition((width - titleWidget.getWidth()) / 2, (listStartY - titleWidget.getHeight()) / 2);
            this.addRenderableWidget(titleWidget);
        }

        this.onInit();
        this.rebuildList();
    }

    public final void rebuildList() {
        if (listLayout != null) listLayout.visitWidgets(this::removeWidget);
        listLayout = new LinearLayout(listStartX, listStartY, LinearLayout.Orientation.VERTICAL);

        this.buildList();

        listLayout.arrangeElements();
        listLayout.visitWidgets(this::addWidget);

        this.listHeight = 0;
        listLayout.visitChildren(c -> this.listHeight += c.getHeight());
        scrollHeight = Math.clamp(scrollHeight, 0, Math.max(listHeight - listVisibleHeight + 10, 0));
        listLayout.setY(listStartY - scrollHeight);
        listLayout.arrangeElements();
    }

    @Override
    public final boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_, double scrollY) {
        if (super.mouseScrolled(p_94686_, p_94687_, p_94688_, scrollY)) return true;
        scrollHeight -= (int) (scrollY * 10.0);
        scrollHeight = Math.clamp(scrollHeight, 0, Math.max(listHeight - listVisibleHeight + 10, 0));
        listLayout.setY(listStartY - scrollHeight);
        listLayout.arrangeElements();
        return true;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.enableScissor(0, listStartY, width, listStartY + listVisibleHeight);
        listLayout.visitWidgets(w -> w.render(guiGraphics, mouseX, mouseY, partialTick));
        guiGraphics.disableScissor();
    }
}
