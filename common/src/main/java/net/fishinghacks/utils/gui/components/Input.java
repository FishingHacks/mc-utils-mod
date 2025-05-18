package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Colors;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Input extends AbstractWidget {
    private static final long CURSOR_BLINK_INTERVAL_MS = 300L;
    public static final int DEFAULT_TEXT_COLOR = Colors.WHITE.get();
    public static final int DEFAULT_TEXT_COLOR_UNEDITABLE = Colors.GRAY.get();
    public static final int DEFAULT_HEIGHT = 16;
    public static final int DEFAULT_WIDTH_SMALL = 90;
    public static final int DEFAULT_WIDTH_NORMAL = 120;
    public static final int DEFAULT_WIDTH_BIG = 150;

    private final Font font;
    private int maxLength = 32;
    private String value = "";
    private boolean isEditable = true;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor = DEFAULT_TEXT_COLOR;
    private int textColorUneditable = DEFAULT_TEXT_COLOR_UNEDITABLE;
    @Nullable
    private Consumer<String> responder = null;
    private Predicate<String> filter = Objects::nonNull;
    @Nullable
    private Component hint = null;
    private long focusTime = Util.getMillis();

    protected Input(Font font, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.font = font;
    }

    public void setValue(String text) {
        if (this.filter.test(text)) {
            if (text.length() > this.maxLength) {
                this.value = text.substring(0, this.maxLength);
            } else {
                this.value = text;
            }

            this.moveCursorToEnd(false);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(text);
        }
    }

    public String getValue() {
        return this.value;
    }

    public void setResponder(@Nullable Consumer<String> responder) {
        this.responder = responder;
    }

    public String getHighlighted() {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(i, j);
    }

    public void setFilter(Predicate<String> validator) {
        this.filter = validator;
    }

    public void insertText(String textToWrite) {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        int k = this.maxLength - this.value.length() - (i - j);
        if (k > 0) {
            String s = StringUtil.filterText(textToWrite);
            int l = s.length();
            if (k < l) {
                if (Character.isHighSurrogate(s.charAt(k - 1))) {
                    k--;
                }

                s = s.substring(0, k);
                l = k;
            }

            String s1 = new StringBuilder(this.value).replace(i, j, s).toString();
            if (this.filter.test(s1)) {
                this.value = s1;
                this.setCursorPosition(i + l);
                this.setHighlightPos(this.cursorPos);
                this.onValueChange(this.value);
            }
        }
    }

    private void onValueChange(String newText) {
        if (this.responder != null) {
            this.responder.accept(newText);
        }
    }

    private void deleteText(int count) {
        if (Screen.hasControlDown()) {
            this.deleteWords(count);
        } else {
            this.deleteChars(count);
        }
    }

    public void deleteWords(int num) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteCharsToPos(this.getWordPosition(num));
            }
        }
    }

    public void deleteChars(int num) {
        this.deleteCharsToPos(this.getCursorPos(num));
    }

    public void deleteCharsToPos(int num) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = Math.min(num, this.cursorPos);
                int j = Math.max(num, this.cursorPos);
                if (i != j) {
                    String s = new StringBuilder(this.value).delete(i, j).toString();
                    if (this.filter.test(s)) {
                        this.value = s;
                        this.moveCursorTo(i, false);
                    }
                }
            }
        }
    }

    public int getWordPosition(int numWords) {
        return this.getWordPosition(numWords, this.getCursorPosition());
    }

    private int getWordPosition(int numWords, int pos) {
        return this.getWordPosition(numWords, pos, true);
    }

    private int getWordPosition(int numWords, int pos, boolean skipConsecutiveSpaces) {
        int i = pos;
        boolean flag = numWords < 0;
        int j = Math.abs(numWords);

        for (int k = 0; k < j; k++) {
            if (!flag) {
                int l = this.value.length();
                i = this.value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (skipConsecutiveSpaces && i < l && this.value.charAt(i) == ' ') {
                        i++;
                    }
                }
            } else {
                while (skipConsecutiveSpaces && i > 0 && this.value.charAt(i - 1) == ' ') {
                    i--;
                }

                while (i > 0 && this.value.charAt(i - 1) != ' ') {
                    i--;
                }
            }
        }

        return i;
    }

    public void moveCursor(int delta, boolean select) {
        this.moveCursorTo(this.getCursorPos(delta), select);
    }

    private int getCursorPos(int delta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, delta);
    }

    public void moveCursorTo(int delta, boolean select) {
        this.setCursorPosition(delta);
        if (!select) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    public void setCursorPosition(int pos) {
        this.cursorPos = Mth.clamp(pos, 0, this.value.length());
        this.scrollTo(this.cursorPos);
    }

    public void moveCursorToStart(boolean select) {
        this.moveCursorTo(0, select);
    }

    public void moveCursorToEnd(boolean select) {
        this.moveCursorTo(this.value.length(), select);
    }

    @Override
    public void setFocused(boolean focused) {
        if (focused) focusTime = Util.getMillis();
        super.setFocused(focused);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isActive() && this.isFocused()) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE:
                    if (this.isEditable) {
                        this.deleteText(-1);
                    }

                    return true;
                case GLFW.GLFW_KEY_DELETE:
                    if (this.isEditable) {
                        this.deleteText(1);
                    }

                    return true;
                case GLFW.GLFW_KEY_RIGHT:
                    if (Screen.hasControlDown()) {
                        this.moveCursorTo(this.getWordPosition(1), Screen.hasShiftDown());
                    } else {
                        this.moveCursor(1, Screen.hasShiftDown());
                    }

                    return true;
                case GLFW.GLFW_KEY_LEFT:
                    if (Screen.hasControlDown()) {
                        this.moveCursorTo(this.getWordPosition(-1), Screen.hasShiftDown());
                    } else {
                        this.moveCursor(-1, Screen.hasShiftDown());
                    }

                    return true;
                case GLFW.GLFW_KEY_LEFT_ALT:
                    this.moveCursorToStart(Screen.hasShiftDown());
                    return true;
                case GLFW.GLFW_KEY_END:
                    this.moveCursorToEnd(Screen.hasShiftDown());
                    return true;
                default:
                    if (Screen.isSelectAll(keyCode)) {
                        this.moveCursorToEnd(false);
                        this.setHighlightPos(0);
                        return true;
                    } else if (Screen.isCopy(keyCode)) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                        return true;
                    } else if (Screen.isPaste(keyCode)) {
                        if (this.isEditable()) {
                            this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                        }

                        return true;
                    } else {
                        if (Screen.isCut(keyCode)) {
                            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                            if (this.isEditable()) {
                                this.insertText("");
                            }

                            return true;
                        }

                        return false;
                    }
            }
        } else {
            return false;
        }
    }

    public boolean canConsumeInput() {
        return this.isActive() && this.isFocused() && this.isEditable();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.canConsumeInput()) return false;
        if (this.isEditable) this.insertText(Character.toString(codePoint));
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int i = Mth.floor(mouseX) - this.getX();

        String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.width);
        this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + this.displayPos, Screen.hasShiftDown());
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
    }

    public void setMaxLength(int length) {
        this.maxLength = length;
        if (this.value.length() > length) {
            this.value = this.value.substring(0, length);
            this.onValueChange(this.value);
        }
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    public void setTextColorUneditable(int color) {
        this.textColorUneditable = color;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean enabled) {
        this.isEditable = enabled;
    }

    public void setHighlightPos(int position) {
        this.highlightPos = Mth.clamp(position, 0, this.value.length());
        this.scrollTo(this.highlightPos);
    }

    private void scrollTo(int position) {
        this.displayPos = Math.min(this.displayPos, this.value.length());
        String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), width);
        int j = s.length() + this.displayPos;
        if (position == this.displayPos) {
            this.displayPos = this.displayPos - this.font.plainSubstrByWidth(this.value, width, true).length();
        }

        if (position > j) {
            this.displayPos += position - j;
        } else if (position <= this.displayPos) {
            this.displayPos = this.displayPos - (this.displayPos - position);
        }

        this.displayPos = Mth.clamp(this.displayPos, 0, this.value.length());
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean isVisible) {
        this.visible = isVisible;
    }

    public int getScreenX(int charNum) {
        return charNum > this.value.length() ? this.getX() : this.getX() + this.font.width(
            this.value.substring(0, charNum));
    }

    public void setHint(@Nullable Component hint) {
        this.hint = hint;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
        if (!isVisible()) return;
        int color = Colors.DARK.get();
        if (isFocused()) color = Colors.DARK_HIGHLIGHT.get();
        if (!isActive() || !this.isEditable) color = Colors.DARK_DISABLED.get();
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Colors.BLACK.get());
        guiGraphics.fill(getX(), getY(), getX() + getWidth() - 1, getY() + getHeight() - 1, color);

        int x = getX() + 3;
        int y = getY() + (height - 8) / 2;
        int textColor = this.isEditable ? this.textColor : textColorUneditable;
        int relativeCursorPos = this.cursorPos - this.displayPos;
        String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.width);
        boolean isCursorVisible = relativeCursorPos >= 0 && relativeCursorPos <= s.length();
        boolean showCursor = isFocused() && (Util.getMillis() - focusTime) / CURSOR_BLINK_INTERVAL_MS % 2L == 0L && isCursorVisible;
        int stringX = x;
        int highlightEnd = Math.clamp(this.highlightPos - this.displayPos, 0, s.length());
        if (!s.isEmpty()) {
            String strToCursor = isCursorVisible ? s.substring(0, relativeCursorPos) : s;
            stringX = guiGraphics.drawString(font, FormattedCharSequence.forward(strToCursor, Style.EMPTY), x, y,
                textColor);
        }

        boolean cursorNotAtEnd = cursorPos < value.length() || value.length() >= getMaxLength();
        int cursorX = stringX;
        if (!isCursorVisible) cursorX = relativeCursorPos > 0 ? x + this.width : x;
        else if (cursorNotAtEnd) {
            cursorX = stringX - 1;
            stringX--;
        }

        if (!s.isEmpty() && isCursorVisible && relativeCursorPos < s.length()) guiGraphics.drawString(this.font,
            FormattedCharSequence.forward(s.substring(relativeCursorPos), Style.EMPTY), stringX, y, textColor);

        if (hint != null && s.isEmpty() && !isFocused())
            guiGraphics.drawString(this.font, this.hint, cursorX, y, textColor);

        if (showCursor) {
            if (cursorNotAtEnd)
                guiGraphics.fill(RenderType.guiOverlay(), cursorX, y - 1, cursorX + 1, y + 10, textColor);
            else guiGraphics.drawString(font, "_", cursorX, y, textColor);
        }

        if (highlightEnd != relativeCursorPos) {
            int start = x + this.font.width(s.substring(0, highlightEnd)) - 1;
            this.renderHighlight(guiGraphics, cursorX, y - 1, start, y + 10);
        }
    }

    private void renderHighlight(GuiGraphics guiGraphics, int minX, int minY, int maxX, int maxY) {
        if (minX < maxX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            int j = minY;
            minY = maxY;
            maxY = j;
        }

        if (maxX > this.getX() + this.width) {
            maxX = this.getX() + this.width;
        }

        if (minX > this.getX() + this.width) {
            minX = this.getX() + this.width;
        }

        guiGraphics.fill(RenderType.guiTextHighlight(), minX, minY, maxX, maxY, -16776961);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage());
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        Component component = this.getMessage();
        return Component.translatable("gui.narrate.editBox", component, this.value);
    }

    public static class Builder {
        Font font;
        @Nullable
        Integer maxLength;
        @Nullable
        String value;
        @Nullable
        Boolean editable;
        @Nullable
        Integer textColor;
        @Nullable
        Integer textColorUneditable;
        @Nullable
        Consumer<String> responder = null;
        Predicate<String> filter = Objects::nonNull;
        @Nullable
        Component hint = null;
        int x;
        int y;
        int width;
        int height;
        Component message;

        public Builder(Font font, int x, int y, int width, int height, Component message) {
            this.font = font;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.message = message;
        }

        public Builder(Font font, int x, int y, int width, int height) {
            this(font, x, y, width, height, Component.empty());
        }

        public Builder(int x, int y, int width, int height, Component message) {
            this(Minecraft.getInstance().font, x, y, width, height, message);
        }

        public Builder(int x, int y, int width, int height) {
            this(Minecraft.getInstance().font, x, y, width, height, Component.empty());
        }

        public static Builder small(int x, int y, Component message) {
            return new Builder(x, y, DEFAULT_WIDTH_SMALL, DEFAULT_HEIGHT, message);
        }

        public static Builder small(int x, int y) {
            return new Builder(x, y, DEFAULT_WIDTH_SMALL, DEFAULT_HEIGHT);
        }
        public static Builder small() {
            return new Builder(0, 0, DEFAULT_WIDTH_SMALL, DEFAULT_HEIGHT);
        }

        public static Builder normal(int x, int y, Component message) {
            return new Builder(x, y, DEFAULT_WIDTH_NORMAL, DEFAULT_HEIGHT, message);
        }

        public static Builder normal(int x, int y) {
            return new Builder(x, y, DEFAULT_WIDTH_NORMAL, DEFAULT_HEIGHT);
        }
        public static Builder normal() {
            return new Builder(0, 0, DEFAULT_WIDTH_NORMAL, DEFAULT_HEIGHT);
        }

        public static Builder big(int x, int y, Component message) {
            return new Builder(x, y, DEFAULT_WIDTH_BIG, DEFAULT_HEIGHT, message);
        }

        public static Builder big(int x, int y) {
            return new Builder(x, y, DEFAULT_WIDTH_BIG, DEFAULT_HEIGHT);
        }
        public static Builder big() {
            return new Builder(0, 0, DEFAULT_WIDTH_BIG, DEFAULT_HEIGHT);
        }

        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder editable(boolean editable) {
            this.editable = editable;
            return this;
        }

        public Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder textColorUneditable(int textColorUneditable) {
            this.textColorUneditable = textColorUneditable;
            return this;
        }

        public Builder responder(Consumer<String> responder) {
            this.responder = responder;
            return this;
        }

        public Builder filter(Predicate<String> filter) {
            this.filter = filter;
            return this;
        }

        public Builder hint(@Nullable Component hint) {
            this.hint = hint;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder message(Component message) {
            this.message = message;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder font(Font font) {
            this.font = font;
            return this;
        }

        public Input build() {
            Input input = new Input(font, x, y, width, height, message);
            if (maxLength != null) input.setMaxLength(maxLength);
            if (value != null) input.setValue(value);
            if (editable != null) input.setEditable(editable);
            if (textColor != null) input.setTextColor(textColor);
            if (textColorUneditable != null) input.setTextColorUneditable(textColorUneditable);
            if (responder != null) input.setResponder(responder);
            input.setFilter(filter);
            input.setHint(hint);

            return input;
        }
    }
}