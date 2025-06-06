package net.fishinghacks.utils.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fishinghacks.utils.gui.components.Button;
import net.fishinghacks.utils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class OverlayScreen extends Screen {
    @Nullable
    protected final Screen parent;
    protected int overlayWidth;
    protected int overlayHeight;
    protected int x;
    protected int y;

    protected OverlayScreen(Component title, @Nullable Screen parent, int width, int height) {
        super(title);
        this.parent = parent;
        this.overlayWidth = width;
        this.overlayHeight = height;
    }

    protected void addCloseButton() {
        addRenderableWidget(
            Button.Builder.cube('<').pos(x - 6 - Button.CUBE_WIDTH, y - 2).onPress(ignored -> onClose()).build());
    }

    @Override
    protected void init() {
        x = (width - overlayWidth) / 2;
        y = (height - overlayHeight) / 2;
        if (parent != null) parent.init(Minecraft.getInstance(), width, height);
        addCloseButton();
        super.init();
    }

    public void setWidth(int overlayWidth) {
        this.overlayWidth = overlayWidth;
        x = (this.width - this.overlayWidth) / 2;
    }

    public void setHeight(int overlayHeight) {
        this.overlayHeight = overlayHeight;
        y = (this.height - overlayHeight) / 2;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return overlayWidth;
    }

    public int getHeight() {
        return overlayHeight;
    }

    public int getRight() {
        return x + overlayWidth;
    }

    public int getBottom() {
        return y + overlayHeight;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        assert minecraft != null;
        if (parent != null) parent.render(guiGraphics, -1, -1, partialTick);
        guiGraphics.flush();
        RenderSystem.getDevice().createCommandEncoder()
            .clearDepthTexture(Objects.requireNonNull(this.minecraft.getMainRenderTarget().getDepthTexture()), 1.0);
        renderTransparentBackground(guiGraphics);
        guiGraphics.fill(x - 2, y - 2, getRight() + 2, getBottom() + 2, Colors.DARK.get());
        guiGraphics.fill(x, y, getRight(), getBottom(), Colors.BG_DARK.get());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
