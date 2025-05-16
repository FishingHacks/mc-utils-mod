package net.fishinghacks.utils.gui.cosmetics;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.caching.DownloadTextureCache;
import net.fishinghacks.utils.caching.FutureState;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import java.io.Closeable;

public class CosmeticsEntry implements Closeable {
    public final String title;
    public final String hash;
    @Nullable
    private ResourceLocation location = null;
    private int width = 0;
    private int height = 0;
    private int maxFrames = 0;
    private int frame = 0;
    long lastFrameTime = 0;
    private final DownloadTextureCache cache;

    public CosmeticsEntry(String title, String hash, DownloadTextureCache cache) {
        this.title = title;
        this.hash = hash;
        this.cache = cache;
    }

    public FutureState<ResourceLocation> state() {
        if (location != null) return FutureState.of(location);
        var state = cache.get(hash);
        if (state.didError()) return FutureState.errored();
        var value = state.getValue();
        if (state.isProcessing() || value.isEmpty()) return FutureState.processing();
        location = Constants.id("cosmetic_gallery/" + Hashing.sha256().hashBytes(hash.getBytes()));
        width = value.get().getWidth();
        height = value.get().getHeight();
        maxFrames = height / (width / 2);
        lastFrameTime = Util.getMillis();
        Constants.LOG.info("Registering {}", location);
        NativeImage original = value.get();
        NativeImage copied = new NativeImage(original.getWidth(), original.getHeight(), true);
        copied.copyFrom(original);
        Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(location::toString, copied));
        // ownership was transferred to the texture manager. This is fine, we have a local copy, so loading it
        // again should not be that expensive.
        return FutureState.of(location);
    }

    public void blit(GuiGraphics graphics, int x, int y, int width, int height) {
        var maybeLocation = state().getValue();
        if (maybeLocation.isEmpty()) return;
        var location = maybeLocation.get();
        if (maxFrames == 0) return;
        if (maxFrames > 1) {
            long time = Util.getMillis();
            if (time > this.lastFrameTime + 100L) {
                this.frame = (this.frame + 1) % this.maxFrames;
                this.lastFrameTime = time;
            }
        }
        int sliceHeight = this.width / 2;
        graphics.blit(RenderType::guiTextured, location, x, y, 0, frame * sliceHeight, width, height, this.width,
            sliceHeight, this.width, this.height, -1);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void close() {
        Constants.LOG.info("Closing {}", location);
        if (location != null) Minecraft.getInstance().getTextureManager().release(location);
        location = null;
    }
}
