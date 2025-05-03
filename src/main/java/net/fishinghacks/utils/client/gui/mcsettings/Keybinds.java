package net.fishinghacks.utils.client.gui.mcsettings;

import com.mojang.blaze3d.platform.InputConstants;
import net.fishinghacks.utils.common.Colors;
import net.fishinghacks.utils.client.gui.Icons;
import net.fishinghacks.utils.client.gui.components.Button;
import net.fishinghacks.utils.client.gui.components.IconButton;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class Keybinds implements OptionSubscreen {
    private static final int VERT_PADDING = 3;
    private static final int HORZ_PADDING = 7;
    private static final int ITEM_HEIGHT = Button.DEFAULT_HEIGHT + VERT_PADDING * 2;
    private static final Map<String, String> cache = new HashMap<>();

    @Nullable
    public KeyMapping selectedKey = null;
    public long lastKeySelection = 0L;
    private InputConstants.Key lastPressedKey;
    private InputConstants.Key lastPressedModifier;
    private boolean isLastKeyHeldDown;
    private boolean isLastModifierHeldDown;
    private final List<KeybindEntry> entries = new ArrayList<>();
    private boolean hasNonDefaultKeymaps = false;

    public Keybinds() {
        lastPressedKey = InputConstants.UNKNOWN;
        lastPressedModifier = InputConstants.UNKNOWN;
        isLastKeyHeldDown = false;
        isLastModifierHeldDown = false;
    }

    private static String getTranslation(String key) {
        return cache.computeIfAbsent(key, k -> I18n.get(k).toLowerCase().trim());
    }

    private static boolean emptyFilter(KeyMapping ignored) {
        return true;
    }

    private static @NotNull Predicate<KeyMapping> getFilter(String filter) {
        filter = filter.trim().toLowerCase();
        if (filter.isEmpty()) return Keybinds::emptyFilter;
        if (filter.startsWith("category:")) {
            final String category = filter.substring(9).trim();
            if (category.isEmpty()) return Keybinds::emptyFilter;
            return mapping -> getTranslation(mapping.getCategory()).contains(category);
        }
        if (filter.startsWith("cat:")) {
            final String category = filter.substring(4).trim();
            if (category.isEmpty()) return Keybinds::emptyFilter;
            return mapping -> getTranslation(mapping.getCategory()).contains(category);
        }
        if (filter.startsWith("key:")) {
            final String key = filter.substring(4).trim();
            if (key.isEmpty()) return Keybinds::emptyFilter;
            return mapping -> getTranslation(mapping.getKey().getName()).contains(key);
        }
        final String name = filter;
        return mapping -> getTranslation(mapping.getName()).contains(name);
    }

    @Override
    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        var filter = getFilter(parent.keybindSearch != null ? parent.keybindSearch.getValue() : "");
        String lastCategory = null;
        Font font = Minecraft.getInstance().font;
        hasNonDefaultKeymaps = false;

        if (!entries.isEmpty()) {
            for (var entry : entries) {
                if (!entry.key.isDefault()) hasNonDefaultKeymaps = true;

                if (!filter.test(entry.key)) continue;
                if (!entry.key.getCategory().equals(lastCategory)) {
                    lastCategory = entry.key.getCategory();
                    layout.addChild(
                        new StringWidget(configWidth, ITEM_HEIGHT, Component.translatable(lastCategory), font));
                }

                layout.addChild(entry);
            }
            return;
        }

        List<KeyMapping> mappings = Arrays.stream(options.keyMappings).sorted().toList();
        for (KeyMapping keyMapping : mappings) {
            if (!keyMapping.isDefault()) hasNonDefaultKeymaps = true;

            if (!filter.test(keyMapping)) continue;
            if (!keyMapping.getCategory().equals(lastCategory)) {
                lastCategory = keyMapping.getCategory();
                layout.addChild(new StringWidget(configWidth, ITEM_HEIGHT, Component.translatable(lastCategory), font));
            }

            entries.add(layout.addChild(new KeybindEntry(keyMapping, keyMapping.getDisplayName(), configWidth)));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, McSettingsScreen parent) {
        if (this.selectedKey == null) return false;
        this.selectedKey.setKey(InputConstants.Type.MOUSE.getOrCreate(button));
        this.selectedKey = null;
        this.refresh();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers, McSettingsScreen parent) {
        if (this.selectedKey == null) return false;
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if (this.lastPressedModifier == InputConstants.UNKNOWN && KeyModifier.isKeyCodeModifier(key)) {
            this.lastPressedModifier = key;
            this.isLastModifierHeldDown = true;
        } else {
            this.lastPressedKey = key;
            this.isLastKeyHeldDown = true;
        }

        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers, McSettingsScreen parent) {
        if (this.selectedKey == null) return false;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.selectedKey.setKeyModifierAndCode(KeyModifier.NONE, InputConstants.UNKNOWN);
            this.selectedKey.setKey(InputConstants.UNKNOWN);
            this.lastPressedKey = InputConstants.UNKNOWN;
            this.lastPressedModifier = InputConstants.UNKNOWN;
            this.isLastKeyHeldDown = false;
            this.isLastModifierHeldDown = false;
        } else {
            InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
            if (this.lastPressedKey.equals(key)) {
                this.isLastKeyHeldDown = false;
            } else if (this.lastPressedModifier.equals(key)) {
                this.isLastModifierHeldDown = false;
            }

            if (this.isLastKeyHeldDown || this.isLastModifierHeldDown) {
                return true;
            }

            if (!this.lastPressedKey.equals(InputConstants.UNKNOWN)) {
                this.selectedKey.setKeyModifierAndCode(KeyModifier.getKeyModifier(this.lastPressedModifier),
                    this.lastPressedKey);
                this.selectedKey.setKey(this.lastPressedKey);
            } else {
                this.selectedKey.setKeyModifierAndCode(KeyModifier.NONE, this.lastPressedModifier);
                this.selectedKey.setKey(this.lastPressedModifier);
            }

            this.lastPressedKey = InputConstants.UNKNOWN;
            this.lastPressedModifier = InputConstants.UNKNOWN;
        }

        this.selectedKey = null;
        this.lastKeySelection = Util.getMillis();
        this.refresh();
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, McSettingsScreen parent) {
        if (parent.keybindResetButton != null) parent.keybindResetButton.active = hasNonDefaultKeymaps;
    }

    private void refresh() {
        KeyMapping.resetMapping();
        entries.forEach(KeybindEntry::refreshEntry);

        hasNonDefaultKeymaps = false;
        var keyMappings = Minecraft.getInstance().options.keyMappings;
        for (KeyMapping keymapping : keyMappings) {
            if (!keymapping.isDefault()) {
                hasNonDefaultKeymaps = true;
                break;
            }
        }
    }

    public class KeybindEntry extends AbstractWidget {
        private final KeyMapping key;
        private final Component name;
        private final Button changeButton;
        private final Button resetButton;

        public KeybindEntry(KeyMapping key, Component name, int width) {
            super(0, 0, width, ITEM_HEIGHT, Component.empty());
            this.key = key;
            this.name = name;
            this.changeButton = Button.Builder.normal(name).onPress(ignored -> {
                Keybinds.this.selectedKey = key;
                Keybinds.this.refresh();
            }).build();
            this.resetButton = new IconButton.Builder(Icons.RESET).active(!key.isDefault()).onPress(ignored -> {
                this.key.setToDefault();
                key.setKey(key.getDefaultKey());
                Keybinds.this.refresh();
            }).build();
            setX(0);
            setY(0);
            refreshEntry();
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Font font = Minecraft.getInstance().font;
            int x = getX() + HORZ_PADDING;
            int y = getY() + (height - font.lineHeight) / 2;
            guiGraphics.drawString(font, name, x, y, Colors.WHITE.get());
        }

        protected void refreshEntry() {
            changeButton.setMessage(this.key.getTranslatedKeyMessage());
            resetButton.active = !this.key.isDefault();
            boolean hasCollision = false;
            MutableComponent collisions = Component.empty();
            if (!this.key.isUnbound()) {
                for (KeyMapping mapping : Minecraft.getInstance().options.keyMappings) {
                    if (mapping == this.key) continue;
                    if (!this.key.same(mapping) && !mapping.hasKeyModifierConflict(this.key)) continue;

                    if (hasCollision) collisions.append(", ");
                    hasCollision = true;
                    collisions.append(mapping.getDisplayName());
                }
            }

            if (hasCollision) {
                this.changeButton.setMessage(Component.literal("[ ")
                    .append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE)).append(" ]")
                    .withStyle(ChatFormatting.RED));
                this.changeButton.setTooltip(
                    Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", collisions)));
            } else changeButton.setTooltip(null);

            if (Keybinds.this.selectedKey == this.key) {
                this.changeButton.setMessage(Component.literal("> ").append(
                        this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                    .append(" <").withStyle(ChatFormatting.YELLOW));
            }
        }

        @Override
        public void setX(int x) {
            super.setX(x);

            int resetX = getRight() - HORZ_PADDING - IconButton.DEFAULT_WIDTH;
            int changeX = resetX - Button.DEFAULT_WIDTH - 6;
            resetButton.setX(resetX);
            changeButton.setX(changeX);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            resetButton.setY(y + VERT_PADDING);
            changeButton.setY(y + VERT_PADDING);
        }

        @Override
        public void setWidth(int width) {
            super.setWidth(width);
            setX(getX());
        }

        @Override
        public void setHeight(int height) {
            super.setHeight(height);
            setY(getY());
        }

        @Override
        public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent ignored) {
            return null;
        }

        @Override
        public @Nullable ComponentPath getCurrentFocusPath() {
            return null;
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> consumer) {
            super.visitWidgets(consumer);
            consumer.accept(changeButton);
            consumer.accept(resetButton);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return false;
        }

        @Override
        protected boolean isValidClickButton(int button) {
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput ignored) {
        }
    }
}
