package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Colors;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class MacroScreenInput extends AbstractTextAreaWidget {
    private static final String CURSOR_APPEND_CHARACTER = "_";
    private static final int CURSOR_BLINK_INTERVAL_MS = 300;
    private final Font font;
    private final Component placeholder;
    private final MultilineTextfield textField;
    private long focusedTime = Util.getMillis();
    private final Consumer<GuiGraphics> onRender;

    public MacroScreenInput(Font font, int x, int y, int width, int height, Component placeholder, Component message,
                            Consumer<GuiGraphics> onRender) {
        super(x, y, width, height, message);
        this.font = font;
        this.placeholder = placeholder;
        this.onRender = onRender;
        this.textField = new MultilineTextfield(font, width - this.totalInnerPadding());
        this.textField.setCursorListener(this::scrollToCursor);
    }

    public void setValueListener(Consumer<String> valueListener) {
        this.textField.setValueListener(valueListener);
    }

    public void setValue(String fullText) {
        this.textField.setValue(fullText);
    }

    public String getValue() {
        return this.textField.value();
    }

    public void updateWidgetNarration(NarrationElementOutput p_259393_) {
        p_259393_.add(NarratedElementType.TITLE,
            Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
    }

    public void onClick(double p_388397_, double p_388871_) {
        this.textField.setSelecting(Screen.hasShiftDown());
        this.seekCursorScreen(p_388397_, p_388871_);
    }

    protected void onDrag(double p_387147_, double p_387062_, double p_388546_, double p_388296_) {
        this.textField.setSelecting(true);
        this.seekCursorScreen(p_387147_, p_387062_);
        this.textField.setSelecting(Screen.hasShiftDown());
    }

    public boolean keyPressed(int p_239433_, int p_239434_, int p_239435_) {
        return this.textField.keyPressed(p_239433_);
    }

    public boolean charTyped(char p_239387_, int p_239388_) {
        if (this.visible && this.isFocused() && StringUtil.isAllowedChatCharacter(p_239387_)) {
            this.textField.insertText(Character.toString(p_239387_));
            return true;
        } else {
            return false;
        }
    }

    public void insertText(String text) {
        this.textField.insertText(text);
    }

    @Override
    protected void renderBackground(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.fill(getX(), getY(), getRight(), getBottom(), Colors.BG_DARK.get());
    }

    protected void renderContents(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        String s = this.textField.value();
        if (s.isEmpty() && !this.isFocused()) {
            graphics.drawWordWrap(this.font, this.placeholder, this.getInnerLeft(), this.getInnerTop(),
                this.width - this.totalInnerPadding(), Colors.GRAY.get());
        } else {
            int cursor = this.textField.cursor();
            boolean showCursor =
                this.isFocused() && (Util.getMillis() - this.focusedTime) / CURSOR_BLINK_INTERVAL_MS % 2L == 0L;
            boolean cursorInView = cursor < s.length();
            int x = 0;
            int cursorY = 0;
            int y = this.getInnerTop();
            onRender.accept(graphics);

            for (MultilineTextfield.StringView lineView : this.textField.iterateLines()) {
                boolean flag2 = this.withinContentAreaTopBottom(y, y + 9);
                if (showCursor && cursorInView && cursor >= lineView.beginIndex() && cursor <= lineView.endIndex()) {
                    if (flag2) {
                        x = font.width(s.substring(lineView.beginIndex(), cursor)) + getInnerLeft();
                        graphics.fill(x, y - 1, x + 1, y + 1 + 9, Colors.WHITE.get());
                    }
                } else {
                    if (flag2) x = font.width(s.substring(lineView.beginIndex(), lineView.endIndex())) + getInnerLeft();

                    cursorY = y;
                }

                y += 9;
            }

            if (showCursor && !cursorInView && this.withinContentAreaTopBottom(cursorY, cursorY + 9)) {
                graphics.drawString(this.font, CURSOR_APPEND_CHARACTER, x, cursorY, Colors.WHITE.get());
            }

            if (this.textField.hasSelection()) {
                MultilineTextfield.StringView selectedStringView = this.textField.getSelected();
                int left = this.getInnerLeft();
                y = this.getInnerTop();

                for (MultilineTextfield.StringView lineView : this.textField.iterateLines()) {
                    if (selectedStringView.beginIndex() <= lineView.endIndex()) {
                        if (lineView.beginIndex() > selectedStringView.endIndex()) break;

                        if (this.withinContentAreaTopBottom(y, y + 9)) {
                            int i1 = this.font.width(s.substring(lineView.beginIndex(),
                                Math.max(selectedStringView.beginIndex(), lineView.beginIndex())));
                            int selectionWidth;
                            if (selectedStringView.endIndex() > lineView.endIndex())
                                selectionWidth = this.width - this.innerPadding();
                            else selectionWidth = this.font.width(
                                s.substring(lineView.beginIndex(), selectedStringView.endIndex()));

                            this.renderHighlight(graphics, left + i1, y, left + selectionWidth, y + 9);
                        }

                    }
                    y += 9;
                }
            }
        }
    }

    protected void renderDecorations(@NotNull GuiGraphics guiGraphics) {
        super.renderDecorations(guiGraphics);
    }

    public int getInnerHeight() {
        return 9 * this.textField.getLineCount();
    }

    protected double scrollRate() {
        return 4.5F;
    }

    private void renderHighlight(GuiGraphics guiGraphics, int minX, int minY, int maxX, int maxY) {
        guiGraphics.fill(RenderType.guiTextHighlight(), minX, minY, maxX, maxY, Colors.BLACK.get());
    }

    private void scrollToCursor() {
        double scrollAmount = this.scrollAmount();
        MultilineTextfield.StringView stringView = this.textField.getLineView((int) (scrollAmount / (double) 9.0F));
        if (this.textField.cursor() <= stringView.beginIndex()) {
            scrollAmount = this.textField.getLineAtCursor() * 9;
        } else {
            MultilineTextfield.StringView lineView = this.textField.getLineView(
                (int) ((scrollAmount + (double) this.height) / (double) 9.0F) - 1);
            if (this.textField.cursor() > lineView.endIndex()) {
                scrollAmount = this.textField.getLineAtCursor() * 9 - this.height + 9 + this.totalInnerPadding();
            }
        }

        this.setScrollAmount(scrollAmount);
    }

    private void seekCursorScreen(double mouseX, double mouseY) {
        double d0 = mouseX - (double) this.getX() - (double) this.innerPadding();
        double d1 = mouseY - (double) this.getY() - (double) this.innerPadding() + this.scrollAmount();
        this.textField.seekCursorToPoint(d0, d1);
    }

    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) this.focusedTime = Util.getMillis();
    }
}
