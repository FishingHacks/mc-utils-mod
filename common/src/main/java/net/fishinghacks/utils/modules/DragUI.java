package net.fishinghacks.utils.modules;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class DragUI extends Screen {
    @Nullable
    private final Screen lastScreen;
    private final Screen clickUi;

    public DragUI(@Nullable Screen lastScreen, @Nullable Screen clickUi) {
        super(Translation.ClickUITitle.get());
        this.lastScreen = lastScreen;
        this.clickUi = clickUi == null ? new ClickUi(lastScreen, this) : clickUi;
    }

    @Override
    protected void init() {
        super.init();

        ModuleManager.modules.forEach((name, module) -> {
            if (module instanceof RenderableModule renderableModule)
                this.addRenderableWidget(new RenderableModulePreview(renderableModule, width, height));
        });
        addRenderableWidget(
            Button.builder(Translation.ClickUITitle.get(), button -> Minecraft.getInstance().setScreen(clickUi))
                .pos((width - 50) / 2, 0).size(50, 20).build());
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null || minecraft.level == null) renderPanorama(guiGraphics, partialTick);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (getFocused() instanceof RenderableModulePreview p) p.renderOutline(guiGraphics);
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(lastScreen);
    }

    private static final class RenderableModulePreview implements GuiEventListener, Renderable, NarratableEntry {
        private final RenderableModule module;
        private final Vector2i size;
        private Vector2i dragStart = new Vector2i(0);
        private boolean dragging;
        private final int screenWidth;
        private final int screenHeight;
        private boolean focused = false;

        private RenderableModulePreview(RenderableModule module, int screenWidth, int screenHeight) {
            this.module = module;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            size = module.previewSize();
        }

        private void clamp() {
            if (module.x < 0) module.x = 0;
            if (module.y < 0) module.y = 0;

            if (module.x + size.x > screenWidth) module.x = screenWidth - size.x;
            if (module.y + size.y > screenHeight) module.y = screenHeight - size.y;
        }

        @Override
        public @NotNull ScreenRectangle getRectangle() {
            return new ScreenRectangle(module.x, module.y, size.x, size.y);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int i, int i1, float partialTick) {
            if (!module.enabled) return;
            clamp();
            guiGraphics.fill(module.x, module.y, module.x + size.x, module.y + size.y, Colors.BLACK.withAlpha(0x7f));
            module.renderPreview(guiGraphics, partialTick);
        }

        public void renderOutline(@NotNull GuiGraphics guiGraphics) {
            if (focused) guiGraphics.renderOutline(module.x - 1, module.y - 1, size.x + 2, size.y + 2,
                Colors.CYAN.withAlpha(0x7f));
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            dragging = false;
            clamp();
            module.savePos();
            return GuiEventListener.super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!isMouseOver(mouseX, mouseY) || button != 0)
                return GuiEventListener.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            dragging = true;
            module.x = (int) mouseX + dragStart.x;
            module.y = (int) mouseY + dragStart.y;
            return true;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && button == 0) {
                dragStart = new Vector2i(module.x - (int) mouseX, module.y - (int) mouseY);
                return true;
            }
            return GuiEventListener.super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (dragging) return true;
            if (mouseX >= module.x && mouseY >= module.y && mouseX < module.x + size.x && mouseY < module.y + size.y)
                return true;
            return GuiEventListener.super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            int amount = (modifiers & GLFW.GLFW_MOD_SHIFT) > 0 ? 10 : 1;
            switch (keyCode) {
                case GLFW.GLFW_KEY_LEFT -> module.x -= amount;
                case GLFW.GLFW_KEY_RIGHT -> module.x += amount;
                case GLFW.GLFW_KEY_UP -> module.y -= amount;
                case GLFW.GLFW_KEY_DOWN -> module.y += amount;
                default -> {
                    return false;
                }
            }
            clamp();
            module.savePos();
            return true;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }

        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public int hashCode() {
            return Objects.hash(module);
        }

        @Override
        public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
            return focused ? null : ComponentPath.leaf(this);
        }

        @Override
        public String toString() {
            return "RenderableModulePreview[" + "module=" + module + ']';
        }

    }
}
