package net.fishinghacks.utils.client.gui.cosmetics;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.client.caching.DownloadTextureCache;
import net.fishinghacks.utils.client.caching.FutureState;
import net.fishinghacks.utils.client.caching.HTTPDownloader;
import net.fishinghacks.utils.common.Utils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.exception.UncheckedException;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// url: https://api.minecraftcapes.net/api/gallery/get?page=N&type={"cape":true,"ears":false,"animated":true}
// encoded: https://api.minecraftcapes.net/api/gallery/get?page=N&type=%7B%22cape%22:true,%22ears%22:false,%22animated%22:true%7D

public class MinecraftCapesGallery {
    public final List<Entry> data;
    public final boolean hasPrevPage;
    public final boolean hasNextPage;
    public final int current_page;

    public static CompletableFuture<MinecraftCapesGallery> fetchPage(int page) {
        if (page < 0) return CompletableFuture.completedFuture(new MinecraftCapesGallery(List.of(), false, true, -1));
        return new HTTPDownloader(
            "https://api.minecraftcapes.net/api/gallery/get?page=" + page + "&type=%7B%22cape%22:true,%22ears%22" +
                ":false,%22animated%22:true%7D").download(
            Util.nonCriticalIoPool()).thenApply(bytes -> {
            try {
                return new Gson().fromJson(new InputStreamReader(new ByteArrayInputStream(bytes)),
                    MinecraftCapesGallery.class);
            } catch (Exception e) {
                throw new UncheckedException(e);
            }
        });
    }

    public List<FetchedEntry> getFetched() {
        return this.data.stream().map(Entry::fetch).toList();
    }

    public MinecraftCapesGallery(List<Entry> data, boolean hasPrevPage, boolean hasNextPage, int currentPage) {
        this.data = data;
        this.hasPrevPage = hasPrevPage;
        this.hasNextPage = hasNextPage;
        current_page = currentPage;
    }

    public record Entry(String hash, String title) {
        public FetchedEntry fetch() {
            return new FetchedEntry(title, hash);
        }
    }

    public static final class FetchedEntry implements Closeable {
        public final String title;
        public final String hash;
        @Nullable
        private ResourceLocation location = null;
        private int width = 0;
        private int height = 0;
        private int maxFrames = 0;
        private int frame = 0;
        long lastFrameTime = 0;

        public FetchedEntry(String title, String hash) {
            this.title = title;
            this.hash = hash;
        }

        public FutureState<ResourceLocation> state() {
            if (location != null) return FutureState.of(location);
            var state = DownloadTextureCache.capeGallery.get(hash);
            if (state.didError()) return FutureState.errored();
            var value = state.getValue();
            if (state.isProcessing() || value.isEmpty()) return FutureState.processing();
            location = Utils.id("cosmetic_gallery/" + hash);
            width = value.get().getWidth();
            height = value.get().getHeight();
            maxFrames = height / (width / 2);
            lastFrameTime = Util.getMillis();
            Utils.getLOGGER().info("Registering {}", location);
            NativeImage original = value.get();
            NativeImage copied = new NativeImage(original.getWidth(), original.getHeight(), true);
            copied.copyFrom(original);
            Minecraft.getInstance().getTextureManager()
                .register(location, new DynamicTexture(location::toString, copied));
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
            Utils.getLOGGER().info("Closing {}", location);
            if (location != null) Minecraft.getInstance().getTextureManager().release(location);
            location = null;
        }
    }
}
