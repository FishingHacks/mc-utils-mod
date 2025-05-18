package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.config.values.Color;
import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ColorInput extends AbstractWidget {
    // 3/2 == 1.5
    private static final int POPUP_PADDING = 10;
    private static final int ELEMENT_PADDING = 5;
    private static final int BAR_WIDTH = 12;
    private static final int PREVIEW_SIZE = 12;
    private static final int GRADIENT_SIZE = Input.DEFAULT_WIDTH_NORMAL - 2 * POPUP_PADDING;
    private static final int ALPHA_BAR_HEIGHT = GRADIENT_SIZE - ELEMENT_PADDING - PREVIEW_SIZE;
    private static final int COLOR_POPUP_WIDTH =
        2 * POPUP_PADDING + GRADIENT_SIZE + 2 * ELEMENT_PADDING + BAR_WIDTH * 2;
    private static final int COLOR_POPUP_HEIGHT = 2 * POPUP_PADDING + GRADIENT_SIZE;

    private static final ResourceLocation HUE_GRADIENT = Constants.id("textures/menu/hue_gradient.png");
    private static final int HUE_GRADIENT_WIDTH = 32;
    private static final int HUE_GRADIENT_HEIGHT = 256;
    private static final ResourceLocation SAT_VAL_GRADIENT = Constants.id(
        "textures/menu/saturation_value_gradient.png");
    private static final int SAT_VAL_GRADIENT_SIZE = 256;

    private final Input child;
    private Color value;
    private @Nullable Consumer<Color> responder = null;
    private int lastOverlayX = 0;
    private int lastOverlayY = 0;

    public ColorInput(Input child, Color value) {
        super(child.getX(), child.getY(), child.getWidth(), child.getHeight(), child.getMessage());
        this.child = child;
        child.setWidth(COLOR_POPUP_WIDTH);
        this.value = value;
        child.setValue(value.format());
        child.setFilter(ColorInput::isValidColorChars);
        child.setResponder(this::onInputChange);
    }

    public ColorInput setValue(Color value) {
        this.value = value;
        this.child.setValue(value.format());
        return this;
    }

    public void setTextColor(int color) {
        child.setTextColor(color);
    }

    public void setResponder(@Nullable Consumer<Color> responder) {
        this.responder = responder;
    }

    private void onInputChange(String text) {
        var col = Color.parse(text);
        if (col.isEmpty()) return;
        value = col.get();
        if (responder != null) responder.accept(value);
    }

    private static boolean isValidColorChars(String text) {
        for (char c : text.toCharArray()) {
            if (c == '#' || (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) continue;
            return false;
        }
        return true;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        child.setFocused(focused);
    }

    @Override
    public boolean isFocused() {
        return child.isFocused();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        child.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        child.setY(y);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        child.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        child.setHeight(height);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return child.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return child.charTyped(codePoint, modifiers);
    }

    private boolean handleOverlayMouse(int x, int y) {
        if (!isFocused() || x < lastOverlayX || y < lastOverlayY || x >= lastOverlayX + COLOR_POPUP_WIDTH || y >= lastOverlayY + COLOR_POPUP_HEIGHT)
            return false;
        int relX = x - lastOverlayX;
        int relY = y - lastOverlayY;
        // Padding
        if (relX < POPUP_PADDING || relY < POPUP_PADDING || relY >= POPUP_PADDING + GRADIENT_SIZE) return false;
        relX -= POPUP_PADDING;
        relY -= POPUP_PADDING;
        // Gradient
        if (relX < GRADIENT_SIZE) {
            float value = 1 - ((float) relY / GRADIENT_SIZE);
            float saturation = (float) relX / GRADIENT_SIZE;
            @SuppressWarnings("SpellCheckingInspection") float[] hsva = this.value.hsva();
            setValue(Color.fromHSVA((int) hsva[0], saturation, value, this.value.a()));
            return true;
        }
        // Padding
        if (relX < ELEMENT_PADDING + GRADIENT_SIZE) return false;
        relX -= ELEMENT_PADDING + GRADIENT_SIZE;
        // Hue Slider
        if (relX < BAR_WIDTH) {
            int hue = relY * 360 / GRADIENT_SIZE;
            @SuppressWarnings("SpellCheckingInspection") float[] hsva = this.value.hsva();
            setValue(Color.fromHSVA(hue, hsva[1], hsva[2], value.a()));
            return true;
        }
        // Padding
        if (relX < ELEMENT_PADDING + BAR_WIDTH) return false;
        relX -= ELEMENT_PADDING + BAR_WIDTH;
        if (relX < BAR_WIDTH && relY < ALPHA_BAR_HEIGHT) {
            int alpha = 255 - (relY * 255 / ALPHA_BAR_HEIGHT);
            setValue(value.withAlpha(alpha));
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (child.mouseClicked(mouseX, mouseY, button)) return true;
        return button == 0 && handleOverlayMouse((int) mouseX, (int) mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return button == 0 && handleOverlayMouse((int) mouseX, (int) mouseY);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return child.createNarrationMessage();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.active = child.active;
        this.visible = child.visible;
        if (!visible) return;
        child.render(guiGraphics, mouseX, mouseY, partialTick);
        if (isFocused()) {
            assert Minecraft.getInstance().screen != null;
            int maxX = Minecraft.getInstance().screen.width - 10;
            var x = getX();
            if (x + COLOR_POPUP_WIDTH > maxX) x = maxX - COLOR_POPUP_WIDTH;
            var y = getBottom() - 1;
            GuiOverlayManager.setOverlay(this, x, y, COLOR_POPUP_WIDTH, COLOR_POPUP_HEIGHT, this::renderOverlay);
        }
    }

    protected void renderOverlay(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int x,
                                 int y) {
        lastOverlayX = x;
        lastOverlayY = y;
        guiGraphics.fill(x, y, x + COLOR_POPUP_WIDTH + 1, y + COLOR_POPUP_HEIGHT + 1, Colors.BLACK.get());
        guiGraphics.fill(x, y, x + COLOR_POPUP_WIDTH, y + COLOR_POPUP_HEIGHT, Colors.DARK.get());
        int hue = (int) value.hsva()[0];

        y += POPUP_PADDING;
        x += POPUP_PADDING;
        guiGraphics.blit(RenderType::guiTextured, SAT_VAL_GRADIENT, x, y, 0, 0, GRADIENT_SIZE, GRADIENT_SIZE,
            SAT_VAL_GRADIENT_SIZE, SAT_VAL_GRADIENT_SIZE, SAT_VAL_GRADIENT_SIZE, SAT_VAL_GRADIENT_SIZE,
            Color.fromHSVA(hue, 1f, 1f, 0xff).argb());
        x += ELEMENT_PADDING + GRADIENT_SIZE;
        guiGraphics.blit(RenderType::guiTextured, HUE_GRADIENT, x, y, 0, 0, BAR_WIDTH, GRADIENT_SIZE,
            HUE_GRADIENT_WIDTH, HUE_GRADIENT_HEIGHT, HUE_GRADIENT_WIDTH, HUE_GRADIENT_HEIGHT, -1);
        x += ELEMENT_PADDING + BAR_WIDTH;
        guiGraphics.renderOutline(x, y, BAR_WIDTH, ALPHA_BAR_HEIGHT, Colors.BG_DARK.get());
        guiGraphics.fillGradient(x + 1, y + 1, x + BAR_WIDTH - 1, y + ALPHA_BAR_HEIGHT - 1, -1, 0);
        y += ALPHA_BAR_HEIGHT + ELEMENT_PADDING;
        guiGraphics.fill(x, y, x + PREVIEW_SIZE, y + PREVIEW_SIZE, value.argb());
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        child.updateWidgetNarration(narrationElementOutput);
    }
}