package net.fishinghacks.utils.gui.screenshots;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.components.Box;
import net.fishinghacks.utils.gui.components.Button;
import net.fishinghacks.utils.gui.components.DummyAbstractWidget;
import net.fishinghacks.utils.gui.components.IconButton;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ScreenshotsBox extends Box {
    public final Button openButton;
    public final Button copyButton;
    public final Button deleteButton;
    public static final int BUTTON_WIDTH = 3 + 2 * 3 + IconButton.DEFAULT_WIDTH * 3;

    public ScreenshotsBox(int x, int y, int width, int height, final ScreenshotsScreen screen, final int id) {
        super(new DummyAbstractWidget());
        openButton = new IconButton.Builder(Icons.OPEN).onPress(ignored -> screen.openScreenshot(id)).build();
        copyButton = new IconButton.Builder(Icons.COPY).onPress(ignored -> screen.copyScreenshot(id)).build();
        deleteButton = new IconButton.Builder(Icons.DELETE).onPress(ignored -> screen.deleteScreenshot(id))
            .color(Colors.RED.get()).build();
        this.setRectangle(width, height, x, y);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        x += width - 3 - IconButton.DEFAULT_WIDTH;
        x -= 2 + IconButton.DEFAULT_WIDTH;
        deleteButton.setX(x);
        x -= 2 + IconButton.DEFAULT_WIDTH;
        copyButton.setX(x);
        x -= 2 + IconButton.DEFAULT_WIDTH;
        openButton.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        y += height - 3 - IconButton.DEFAULT_HEIGHT;
        openButton.setY(y);
        copyButton.setY(y);
        deleteButton.setY(y);
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
        consumer.accept(openButton);
        consumer.accept(copyButton);
        consumer.accept(deleteButton);
    }
}
