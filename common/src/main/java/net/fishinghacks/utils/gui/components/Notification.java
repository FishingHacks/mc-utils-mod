package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.fishinghacks.utils.Colors;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

public class Notification extends UnfocusableWidget {
    public static final int DEFAULT_WIDTH = 200;
    public static final int DEFAULT_HEIGHT = 30;
    public static final int buttonPadding = 2;
    public static final int textPadding = 2;

    private long startMillis;
    private long lastRenderMillis = Util.getMillis();
    private final long durationMillis;
    private final Button closeButton = Button.Builder.cube('×').size(12, 12).onPress(ignored -> close()).build();
    private final List<Button> buttons;
    private int textWidth;
    private int textHeight;
    private int textOffsetY;
    private @Nullable Consumer<Notification> onClose;

    public static int getHeight(Component message, int width, boolean hasButtons) {
        width = width - 2 - textPadding * 2 - buttonPadding * 2 - Button.CUBE_WIDTH;
        int height = Minecraft.getInstance().font.wordWrapHeight(message, width);
        height += textPadding * 2 + 6;
        if (hasButtons) height += buttonPadding * 2 + Button.DEFAULT_HEIGHT;
        return height;
    }

    public Notification(Component message, List<NotifyButton> buttons, long durationMillis) {
        super(0, 0, DEFAULT_WIDTH, getHeight(message, DEFAULT_WIDTH, !buttons.isEmpty()), message);
        this.startMillis = Util.getMillis();
        this.durationMillis = durationMillis;
        this.buttons = buttons.stream()
            .map(btn -> Button.Builder.normal(btn.name).height(Button.DEFAULT_HEIGHT - 2).onPress(btn.onClick).build())
            .toList();
        repositionWidgets();
    }

    public Notification(Component message, List<NotifyButton> buttons, Duration duration) {
        this(message, buttons, duration.toMillis());
    }

    public void onClose(@Nullable Consumer<Notification> onClose) {
        this.onClose = onClose;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        repositionWidgets();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        repositionWidgets();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        repositionWidgets();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        repositionWidgets();
    }

    @Override
    public void setPosition(int x, int y) {
        super.setX(x);
        super.setY(y);
        repositionWidgets();
    }

    void repositionWidgets() {
        int x = getX();
        int y = getY();
        int width = getWidth();
        closeButton.setX(getRight() - 1 - buttonPadding - closeButton.getWidth());
        closeButton.setY(y + 2 + buttonPadding);
        textWidth = width - 2 - textPadding * 2 - buttonPadding * 2 - closeButton.getWidth();
        textHeight = getHeight() - 3 - textPadding * 2;
        if (!buttons.isEmpty()) {
            textHeight -= 2 * buttonPadding + Button.DEFAULT_HEIGHT;
            int buttonWidth = (width - 2) / buttons.size();
            for (int i = 0; i < buttons.size(); ++i) {
                Button button = buttons.get(i);
                button.setY(getBottom() - 1 - buttonPadding - Button.DEFAULT_HEIGHT);
                button.setX(x + 1 + buttonWidth * i + buttonPadding);
                button.setWidth(buttonWidth - 2 * buttonPadding);
            }
        }
        int actualTextHeight = Minecraft.getInstance().font.wordWrapHeight(getMessage(), textWidth);
        textOffsetY = Math.max(0, textHeight - actualTextHeight);
    }

    public boolean checkForRemoval() {
        if (Util.getMillis() - startMillis > durationMillis) {
            close();
            return true;
        }
        return false;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered) startMillis += Util.getMillis() - lastRenderMillis;
        lastRenderMillis = Util.getMillis();

        RenderType overlay = RenderType.guiOverlay();
        guiGraphics.fill(overlay, getX(), getY(), getRight(), getBottom(), Colors.DARK.get());
        guiGraphics.fill(overlay, getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, Colors.BG_DARK.get());
        int millisSinceStart = (int) Math.min(Util.getMillis() - startMillis, durationMillis);
        int progress = millisSinceStart * getWidth() / (int) durationMillis;
        guiGraphics.fill(overlay, getX(), getY(), getX() + progress, getY() + 2,
            isHovered ? Colors.GRAY.get() : Colors.DARK_HIGHLIGHT.get());

        int textX = getX() + 1 + textPadding;
        int textY = getY() + 2 + textPadding;
        guiGraphics.enableScissor(textX, textY, textX + textWidth, textY + textHeight);
        guiGraphics.drawWordWrap(Minecraft.getInstance().font, getMessage(), textX, textY + textOffsetY, textWidth,
            Colors.WHITE.get());
        guiGraphics.disableScissor();

        closeButton.render(overlay, guiGraphics, mouseX, mouseY);
        for (var btn : buttons) btn.render(overlay, guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closeButton.mouseClicked(mouseX, mouseY, button)) return true;
        for (var btn : buttons) if (btn.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }

    public void close() {
        GuiOverlayManager.removeNotification(this);
        var onClose = this.onClose;
        if (onClose != null) onClose.accept(this);
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
        super.visitWidgets(consumer);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }

    public record NotifyButton(Component name, Consumer<Button> onClick) {
    }
}
