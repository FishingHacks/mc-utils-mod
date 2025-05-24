package net.fishinghacks.utils.gui;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.gui.components.Button;
import net.fishinghacks.utils.gui.components.GuiDropdown;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropdownPopupScreen<T> extends OverlayScreen {
    public final Consumer<T> consumer;
    private GuiDropdown<T> input;
    private final T initialValue;
    private final List<T> validValues;
    private final Function<T, Component> stringify;
    private final int titleWidth;

    protected DropdownPopupScreen(@Nullable Screen parent, Component title, Consumer<T> consumer, T value,
                                  List<T> validValues, Function<T, Component> stringify) {
        super(title, parent, 250, 80);
        this.consumer = consumer;
        this.initialValue = value;
        titleWidth = Minecraft.getInstance().font.width(title);
        this.validValues = validValues;
        this.stringify = stringify;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.Builder.small(CommonComponents.GUI_OK).onPress(ignored -> onSubmit())
            .pos(width / 2 - 2 - Button.SMALL_WIDTH, getBottom() - 5 - Button.DEFAULT_HEIGHT).build());
        addRenderableWidget(Button.Builder.small(CommonComponents.GUI_CANCEL).onPress(ignored -> onClose())
            .pos(width / 2 + 2, getBottom() - 5 - Button.DEFAULT_HEIGHT).build());
        int insideY = getY() + 5 + font.lineHeight;
        int insideHeight = overlayHeight - 5 - font.lineHeight - 5 - Button.DEFAULT_HEIGHT;

        input = new GuiDropdown<>(input != null ? input.getValue() : initialValue, stringify, validValues);
        input.setWidth(200);
        input.setPosition((width - 200) / 2, insideY + (insideHeight - GuiDropdown.DEFAULT_HEIGHT) / 2);
        addRenderableWidget(input);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int p_281550_, int p_282878_, float p_282465_) {
        super.render(guiGraphics, p_281550_, p_282878_, p_282465_);

        guiGraphics.drawString(font, getTitle(), (width - titleWidth) / 2, getY() + 5, Colors.WHITE.get());
    }

    public void onSubmit() {
        onClose();
        if (input != null) consumer.accept(input.getValue());
    }

    public static <T> void open(Component title, Consumer<T> consumer, T value, List<T> validValues,
                                Function<T, Component> stringify) {
        Minecraft.getInstance().setScreen(
            new DropdownPopupScreen<>(Minecraft.getInstance().screen, title, consumer, value, validValues, stringify));
    }
}
