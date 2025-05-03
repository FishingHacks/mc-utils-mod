package net.fishinghacks.utils.client.modules;

import com.mojang.blaze3d.platform.InputConstants;
import net.fishinghacks.utils.common.Colors;
import net.fishinghacks.utils.common.Translation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClickUi extends Screen {
    public static final Lazy<KeyMapping> CLICK_UI_MAPPING = Lazy.of(() -> new KeyMapping(Translation.ClickUIOpenKey.key(), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.misc"));

    @Nullable
    private final Screen lastScreen;

    public ClickUi(@Nullable Screen lastScreen) {
        super(Translation.ClickUITitle.get());
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();

        int x = 100;
        for (ModuleCategory cat : ModuleCategory.values()) {
            addRenderableWidget(new ClickUiCategoryButton(cat, x, 40));
            x += 140;
        }
        addRenderableWidget(Button.builder(Translation.DragUITitle.get(), button -> Minecraft.getInstance().setScreen(new DragUI(lastScreen))).pos((width - 50) / 2, 0).size(50, 20).build());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(lastScreen);
    }

    private static class ClickUiCategoryButton implements GuiEventListener, Renderable, NarratableEntry {
        private final ModuleCategory category;
        private final List<Module> modules = new ArrayList<>();
        private int x;
        private int y;
        private Vector2i dragStart = new Vector2i(0);
        boolean extended = false;
        boolean isDragging = false;

        private ClickUiCategoryButton(ModuleCategory category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;

            ModuleManager.modules.forEach((name, module) -> {
                if (module.category() == category) modules.add(module);
            });
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            int lineHeight = font.lineHeight + 4;

            guiGraphics.fill(x + 1, y + 1, x + 99, y + font.lineHeight + 4 - 1, Colors.PRIMARY.get());
            guiGraphics.drawCenteredString(font, category.component, x + 50, y + 2, Colors.WHITE.get());
            guiGraphics.fill(x + 1, y, x + 99, y + 1, Colors.PRIMARY.get());
            guiGraphics.fill(x + 1, y + lineHeight - 1, x + 99, y + lineHeight, Colors.PRIMARY.get());
            guiGraphics.fill(x, y + 1, x + 1, y + lineHeight - 1, Colors.PRIMARY.get());
            guiGraphics.fill(x + 99, y + 1, x + 100, y + lineHeight - 1, Colors.PRIMARY.get());
            if (!extended) return;
            int y = this.y + lineHeight - 1;

            int height = lineHeight * modules.size() + 1;
            guiGraphics.fill(x + 1, y, x + 99, y + height - 1, Colors.SECONDARY.get());
            guiGraphics.fill(x, y, x + 1, y + height - 1, Colors.PRIMARY.get());
            guiGraphics.fill(x + 99, y, x + 100, y + height - 1, Colors.PRIMARY.get());
            guiGraphics.fill(x + 1, y + height - 1, x + 99, y + height, Colors.PRIMARY.get());

            if (!extended) return;
            if (mouseX >= x && mouseX < x + 100 && mouseY >= y && mouseY < y + height) {
                int index = (mouseY - y) / lineHeight;
                if (index < modules.size())
                    guiGraphics.fill(x + 1, y + lineHeight * index, x + 99, y + lineHeight * (index + 1), Colors.SECONDARY_DARK.get());
            }

            for (int i = 0; i < modules.size(); ++i) {
                int color = Colors.WHITE.get();
                if (modules.get(i).enabled) color = Colors.CYAN.get();
                guiGraphics.drawString(font, Component.translatable("utils.configuration." + modules.get(i).name()), x + 5, y + 2 + lineHeight * i, color);
            }
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            this.isDragging = false;

            return GuiEventListener.super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if ((new Rect2i(x, y, 100, Minecraft.getInstance().font.lineHeight + 4).contains((int) mouseX, (int) mouseY) && button == 0) || isDragging) {
                this.x = (int) mouseX + dragStart.x;
                this.y = (int) mouseY + dragStart.y;
                this.isDragging = true;
                return true;
            }

            return GuiEventListener.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isDragging) return true;
            int lineHeight = Minecraft.getInstance().font.lineHeight + 4;
            if (new Rect2i(x, y, 100, lineHeight).contains((int) mouseX, (int) mouseY)) {
                if (button == 1) this.extended = !this.extended;
                else if (button == 0) dragStart = new Vector2i(x - (int) mouseX, y - (int) mouseY);
                return true;
            }

            if (new Rect2i(x, y + lineHeight, 100, lineHeight * modules.size()).contains((int) mouseX, (int) mouseY)) {
                int idx = ((int) mouseY - y - lineHeight) / lineHeight;
                ModuleManager.toggleModule(modules.get(idx).name());
                return true;
            }

            return GuiEventListener.super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (isDragging) return true;
            int lineHeight = Minecraft.getInstance().font.lineHeight + 4;
            int height = lineHeight;
            if (extended) {
                height += lineHeight * modules.size();
            }
            return new Rect2i(x, y, 100, height).contains((int) mouseX, (int) mouseY) || GuiEventListener.super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void setFocused(boolean b) {
        }

        @Override
        public boolean isFocused() {
            return false;
        }

        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput ignored) {

        }
    }
}
