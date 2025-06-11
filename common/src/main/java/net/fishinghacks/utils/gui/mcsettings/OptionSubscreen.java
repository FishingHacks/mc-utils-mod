package net.fishinghacks.utils.gui.mcsettings;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LinearLayout;

interface OptionSubscreen {
    void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent);

    @SuppressWarnings("unused")
    default void onClose(Options options, McSettingsScreen parent) {
    }

    @SuppressWarnings("unused")
    default boolean mouseClicked(double mouseX, double mouseY, int button, McSettingsScreen parent) {
        return false;
    }

    @SuppressWarnings("unused")
    default boolean keyPressed(int keyCode, int scanCode, int modifiers, McSettingsScreen parent) {
        return false;
    }

    @SuppressWarnings("unused")
    default boolean keyReleased(int keyCode, int scanCode, int modifiers, McSettingsScreen parent) {
        return false;
    }

    @SuppressWarnings("unused")
    default void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, McSettingsScreen parent) {}
}
