package net.fishinghacks.utils.gui.mcsettings;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LinearLayout;

public interface OptionSubscreen {
    void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent);

    default void onClose(Options options, McSettingsScreen parent) {
    }

    default boolean mouseClicked(double mouseX, double mouseY, int button, McSettingsScreen parent) {
        return false;
    }

    default boolean keyPressed(int keyCode, int scanCode, int modifiers, McSettingsScreen parent) {
        return false;
    }

    default boolean keyReleased(int keyCode, int scanCode, int modifiers, McSettingsScreen parent) {
        return false;
    }

    default void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, McSettingsScreen parent) {}
}
