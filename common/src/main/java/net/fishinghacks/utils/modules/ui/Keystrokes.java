package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.RenderableModule;
import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;

@Module(name = "keystrokes", category = ModuleCategory.UI)
public class Keystrokes extends RenderableModule {
    public static final int buttonSize = 17;
    public static final int textOff = 5;
    public static final int buttonSpacing = 4;
    public static final int width = 2 * buttonSpacing + 3 * buttonSize;
    public static final int spaceHeight = 10;
    public static final int height = 2 * buttonSpacing + 2 * buttonSize + spaceHeight;
    public static final int heightMouse = height + buttonSize + buttonSpacing;
    public static final int mouseBtnWidth = (width - buttonSpacing) / 2;

    private CachedValue<Boolean> showMouse;

    @Override
    public void buildConfig(Config cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);
        showMouse = CachedValue.wrap(cfg, builder, "keystrokes_show_mouse", false);
    }

    private void drawButton(GuiGraphics graphics, int x, int y, boolean pressed, Component key) {
        graphics.fill(x, y, x + buttonSize, y + buttonSize, (pressed ? Colors.WHITE : Colors.BLACK).withAlpha(0x7f));
        graphics.drawString(Minecraft.getInstance().font, key,
            x + (buttonSize - Minecraft.getInstance().font.width(key)) / 2 + 1, y + textOff,
            pressed ? Colors.BLACK.get() : Colors.WHITE.get(), false);
    }

    private void drawMouseButton(GuiGraphics graphics, int x, int y, boolean pressed, Component key) {
        graphics.fill(x, y, x + mouseBtnWidth, y + buttonSize, (pressed ? Colors.WHITE : Colors.BLACK).withAlpha(0x7f));
        graphics.drawString(Minecraft.getInstance().font, key,
            x + (mouseBtnWidth - Minecraft.getInstance().font.width(key)) / 2 + 1, y + textOff,
            pressed ? Colors.BLACK.get() : Colors.WHITE.get(), false);
    }

    public void render(GuiGraphics graphics, boolean fwd, boolean left, boolean back, boolean right, boolean space,
                       boolean lmb, boolean rmb) {
        int pressed = Colors.WHITE.withAlpha(0x7f);
        int released = Colors.BLACK.withAlpha(0x7f);
        int pressedText = Colors.BLACK.get();
        int releasedText = Colors.WHITE.get();
        var options = Minecraft.getInstance().options;

        int y = this.y;
        int x = this.x + buttonSize + buttonSpacing;
        drawButton(graphics, x, y, fwd, options.keyUp.getTranslatedKeyMessage());

        y += buttonSize + buttonSpacing;
        x = this.x;
        drawButton(graphics, x, y, left, options.keyLeft.getTranslatedKeyMessage());
        x += buttonSize + buttonSpacing;
        drawButton(graphics, x, y, back, options.keyDown.getTranslatedKeyMessage());
        x += buttonSize + buttonSpacing;
        drawButton(graphics, x, y, right, options.keyRight.getTranslatedKeyMessage());

        y += buttonSize + buttonSpacing;
        x = this.x;
        graphics.fill(x, y, x + width, y + spaceHeight, space ? pressed : released);
        x += (width - buttonSize) / 2;
        graphics.fill(x, y + 3, x + buttonSize, y + 4, space ? pressedText : releasedText);
        if (!showMouse.get()) return;

        y += spaceHeight + buttonSpacing;
        x = this.x;
        drawMouseButton(graphics, x, y, lmb, Translation.Lmb.get());
        x += mouseBtnWidth + buttonSpacing;
        drawMouseButton(graphics, x, y, rmb, Translation.Rmb.get());
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        var options = Minecraft.getInstance().options;
        render(guiGraphics, options.keyUp.isDown(), options.keyLeft.isDown(), options.keyDown.isDown(),
            options.keyRight.isDown(), options.keyJump.isDown(), options.keyAttack.isDown(), options.keyUse.isDown());
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, float partialTick) {
        render(guiGraphics, false, false, false, false, false, false, false);
    }

    @Override
    public Vector2i previewSize() {
        return new Vector2i(width, showMouse.get() ? heightMouse : height);
    }
}
