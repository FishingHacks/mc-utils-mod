package net.fishinghacks.utils.gui;

import net.fishinghacks.utils.gui.components.Button;
import net.fishinghacks.utils.gui.components.Input;
import net.fishinghacks.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class InputPopupScreen extends OverlayScreen {
    public final Consumer<String> consumer;
    @Nullable
    private final Component hint;
    private Input input;
    private final String initialValue;
    private final int titleWidth;

    protected InputPopupScreen(@Nullable Screen parent, Component title, @Nullable Component hint, Consumer<String> consumer,
                               String value) {
        super(title, parent, 250, 80);
        this.consumer = consumer;
        this.initialValue = value;
        titleWidth = Minecraft.getInstance().font.width(title);
        this.hint = hint;
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

        input = addRenderableWidget(
            Input.Builder.big().value(input != null ? input.getValue() : initialValue).width(200)
                .pos((width - 200) / 2, insideY + (insideHeight - Input.DEFAULT_HEIGHT) / 2).hint(hint).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int p_281550_, int p_282878_, float p_282465_) {
        super.render(guiGraphics, p_281550_, p_282878_, p_282465_);

        guiGraphics.drawString(font, getTitle(), (width - titleWidth) / 2, getY() + 5, Colors.WHITE.get());
    }

    public void onSubmit() {
        if (input != null) consumer.accept(input.getValue());
        onClose();
    }

    public static void open(Component message, Component hint, Consumer<String> consumer, String value) {
        Minecraft.getInstance()
            .setScreen(new InputPopupScreen(Minecraft.getInstance().screen, message, hint, consumer, value));
    }

    public static void open(Component message, Component hint, Consumer<String> consumer) {
        open(message, hint, consumer, "");
    }

    public static void open(Component message, Consumer<String> consumer, String value) {
        open(message, Component.empty(), consumer, value);
    }

    public static void open(Component message, Consumer<String> consumer) {
        open(message, Component.empty(), consumer, "");
    }
}
