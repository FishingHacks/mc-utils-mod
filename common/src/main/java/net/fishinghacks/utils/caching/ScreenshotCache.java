package net.fishinghacks.utils.caching;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ScreenshotCache {
    public static final ScreenshotCache instance = new ScreenshotCache();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final LoadingCache<String, CompletableFuture<NativeImage>> cache;

    private ScreenshotCache() {
        cache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(60))
            .removalListener(ScreenshotCache::onRemove).build(new CacheLoader<>() {
                @Override
                public @NotNull CompletableFuture<NativeImage> load(@NotNull String key) {
                    return CompletableFuture.supplyAsync(() -> {
                        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots")
                            .resolve(key + ".png");
                        try (InputStream stream = Files.newInputStream(path)) {
                            return NativeImage.read(stream);
                        } catch (IOException e) {
                            LOGGER.info("Failed to load screenshot {}", key, e);
                            throw new UncheckedIOException(e);
                        }
                    }, Util.nonCriticalIoPool());
                }
            });
    }

    public CompletableFuture<NativeImage> getOrLoad(String key) {
        return cache.getUnchecked(key);
    }

    private static void onRemove(RemovalNotification<String, CompletableFuture<NativeImage>> notification) {
        var future = notification.getValue();
        if (future == null) return;
        if (!future.isDone()) future.thenAccept(ScreenshotCache::delete);
        else try {
            delete(future.get());
        } catch (Exception ignored) {
        }
    }

    private static void delete(NativeImage img) {
        try {
            img.close();
        } catch (Exception ignored) {
        }
    }
}
