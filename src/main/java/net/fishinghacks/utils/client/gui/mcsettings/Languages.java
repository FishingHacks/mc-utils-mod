package net.fishinghacks.utils.client.gui.mcsettings;

import net.fishinghacks.utils.common.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Languages implements OptionSubscreen {
    private static final int ITEM_HEIGHT = 30;

    private String language = "";
    private @Nullable Entry focused = null;

    @Override
    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        var languageManager = Minecraft.getInstance().getLanguageManager();
        language = languageManager.getSelected();
        final int[] offset = {0, -1};
        languageManager.getLanguages().forEach((code, info) -> {
            var widget = layout.addChild(new Entry(configWidth - 40, code, info.toComponent()));
            if (this.language.equals(code)) {
                offset[1] = offset[0];
                parent.setFocused(widget);
                focused = widget;
            }
            offset[0] += ITEM_HEIGHT;
        });
        if (offset[1] >= 0) parent.scrollToHeight(offset[1] + (ITEM_HEIGHT + parent.getListVisibleHeight()) / 2, false);
    }

    @Override
    public void onClose(Options options, McSettingsScreen parent) {
        if (language.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        var languageManager = mc.getLanguageManager();
        if (language.equals(languageManager.getSelected())) return;
        languageManager.setSelected(language);
        options.languageCode = language;
        mc.reloadResourcePacks();
    }

    private final class Entry extends AbstractWidget {
        private final String code;
        private final Component language;
        private boolean isSelected = false;

        public Entry(int width, String code, Component language) {
            super(0, 0, width, ITEM_HEIGHT, language);
            this.code = code;
            this.language = language;
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
            if (isSelected) {
                guiGraphics.fill(getX() + 9, getY() + 2, getRight() - 9, getBottom() - 2, Colors.WHITE.get());
                guiGraphics.fill(getX() + 10, getY() + 3, getRight() - 10, getBottom() - 3, Colors.BLACK.get());
            }
            Font font = Minecraft.getInstance().font;
            int offsetX = getX() + (width - font.width(language)) / 2;
            int offsetY = getY() + (height - font.lineHeight + 2) / 2;
            guiGraphics.drawString(font, language, offsetX, offsetY, Colors.WHITE.get());
        }

        @Override
        public void setFocused(boolean focused) {
            if (focused) {
                if (Languages.this.focused != null) Languages.this.focused.isSelected = false;
                isSelected = true;
                Languages.this.language = this.code;
                Languages.this.focused = this;
            }

            super.setFocused(focused);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        }
    }
}
