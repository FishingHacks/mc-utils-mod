package net.fishinghacks.utils.modules;

import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2i;

public abstract class RenderableModule extends IModule {
    public int x = 0;
    public int y = 0;

    private CachedValue<Integer> posX;
    private CachedValue<Integer> posY;

    public void savePos() {
        posX.set(x, false);
        posY.set(y);
    }

    @Override
    public void buildConfig(AbstractConfig cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);
        posX = CachedValue.wrap(cfg, builder, "x", 0);
        posY = CachedValue.wrap(cfg, builder, "y", 0);
        posX.onInvalidate(() -> x = posX.get());
        posY.onInvalidate(() -> y = posY.get());
    }

    public abstract void render(GuiGraphics guiGraphics, float partialTick);

    public boolean shouldRender() {
        if(!isEnabled()) return false;
        var dbg = Minecraft.getInstance().gui.getDebugOverlay();
        boolean hideHalf = dbg.showDebugScreen();
        if (hideHalf && (dbg.showFpsCharts() || dbg.showNetworkCharts() || dbg.showProfilerChart())) return false;
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        return !hideHalf || posY.get() > height / 2;
    }

    public abstract void renderPreview(GuiGraphics guiGraphics, float partialTick);

    public abstract Vector2i previewSize();

    protected final Vector2i getPosition(int width, int height) {
        int maxWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int maxHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        Vector2i pos = new Vector2i(x, y);
        if (pos.x + width > maxWidth) pos.x = maxWidth - width;
        if (pos.y + height > maxHeight) pos.y = maxHeight - height;
        return pos;
    }
}
