package net.fishinghacks.utils.client.gui;

import net.fishinghacks.utils.common.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class BlackScreen extends Screen {
    protected final Screen parent;

    protected BlackScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, Colors.BG_DARK.get());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
