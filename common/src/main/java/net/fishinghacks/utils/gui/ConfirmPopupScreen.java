package net.fishinghacks.utils.gui;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.gui.components.Button;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfirmPopupScreen extends OverlayScreen {
    private final int titleWidth;
    private final Runnable onConfirm;

    protected ConfirmPopupScreen(@Nullable Screen parent, Component message, Runnable onConfirm) {
        super(message, parent, 250, 70);
        titleWidth = Math.min(Minecraft.getInstance().font.width(message), overlayWidth - 8);
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.Builder.small(CommonComponents.GUI_OK).onPress(ignored -> onConfirm())
            .pos(width / 2 - 2 - Button.SMALL_WIDTH, getBottom() - 5 - Button.DEFAULT_HEIGHT).build());
        addRenderableWidget(Button.Builder.small(CommonComponents.GUI_CANCEL).onPress(ignored -> onClose())
            .pos(width / 2 + 2, getBottom() - 5 - Button.DEFAULT_HEIGHT).build());
    }

    @Override
    protected void addCloseButton() {
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int p_281550_, int p_282878_, float p_282465_) {
        super.render(guiGraphics, p_281550_, p_282878_, p_282465_);
        guiGraphics.drawWordWrap(font, getTitle(), getX() + (overlayWidth - titleWidth) / 2, getY() + 5,
            overlayWidth - 8, Colors.WHITE.get());
    }

    public void onConfirm() {
        onClose();
        onConfirm.run();
    }

    public static void open(Component message, Runnable onConfirm) {
        Minecraft.getInstance().setScreen(new ConfirmPopupScreen(Minecraft.getInstance().screen, message, onConfirm));
    }
}
