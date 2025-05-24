package net.fishinghacks.utils.gui.components;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class ConfigSection implements LayoutElement {
    public static final int DEFAULT_MARGIN = 5;

    final Text header;
    @Nullable
    final Text footer;
    final AbstractWidget widget;
    final int width;
    final int margin;

    public ConfigSection(int margin, int x, int y, Component header, @Nullable Component footer,
                         AbstractWidget widget, int width) {
        this.width = width;
        this.margin = margin;
        this.widget = widget;
        widget.setX(x + width - widget.getWidth() - margin);
        widget.setY(margin + y);
        widget.setWidth(Math.min(width - 2 * margin, widget.getWidth()));
        this.header = new Text.Builder(header.copy()).pos(margin + x, margin + y).width(width - 2 * margin).scale(1.5f)
            .build();
        int height = Math.max(widget.getHeight(), this.header.getHeight()) + 6;
        if (footer == null) this.footer = null;
        else this.footer = new Text.Builder(footer.copy()).pos(margin + x, margin + y + height).width(width - 2 * margin)
            .build();
    }


    public ConfigSection(Component header, @Nullable Component footer, AbstractWidget widget, int width) {
        this(DEFAULT_MARGIN, 0, 0, header, footer, widget, width);
    }

    public ConfigSection(Component header, AbstractWidget widget, int width) {
        this(DEFAULT_MARGIN, 0, 0, header, null, widget, width);
    }

    public AbstractWidget getWidget() {
        return widget;
    }

    @Override
    public void setX(int x) {
        header.setX(margin + x + 2);
        widget.setX(x + width - widget.getWidth() - margin);
        if (footer != null) footer.setX(margin + x);
    }

    @Override
    public void setY(int y) {
        header.setY(margin + y);
        widget.setY(margin + y);
        if (footer != null) footer.setY(margin + y + 6 + Math.max(header.getHeight(), widget.getHeight()));
    }

    @Override
    public int getX() {
        return header.getX() - margin;
    }

    @Override
    public int getY() {
        return header.getY() - margin;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        int height = Math.max(header.getHeight(), widget.getHeight()) + margin * 2 + 6;
        if (footer == null) return height;
        return height + footer.getHeight();
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(header);
        if (footer != null) consumer.accept(footer);
        consumer.accept(widget);
    }

}
