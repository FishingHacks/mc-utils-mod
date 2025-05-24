package net.fishinghacks.utils.caching;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.mojang.logging.LogUtils;
import net.fishinghacks.utils.ClientConstants;
import net.minecraft.FileUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@MethodsReturnNonnullByDefault
public class GenericCache<K, V> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path root;
    private final LoadingCache<K, CompletableFuture<V>> cache;
    private final CacheType<K, V> type;

    public GenericCache(CacheType<K, V> type, Duration expiresAfter) {
        this.type = type;
        this.root = ClientConstants.dataDirectory.get().resolve(type.getFolderName());
        cache = CacheBuilder.newBuilder().expireAfterAccess(expiresAfter).removalListener(GenericCache::onRemove)
            .build(new CacheLoader<>() {
                @Override
                public CompletableFuture<V> load(@NotNull K key) {
                    return GenericCache.this.downloadBytes(key).thenCompose(type::process).exceptionallyCompose(e -> {
                        LOGGER.info("Failed to load {}", key, e);
                        return CompletableFuture.failedStage(e);
                    });
                }
            });
    }

    public GenericCache(CacheType<K, V> type) {
        this(type, Duration.ofSeconds(120));
    }

    private static void onRemove(RemovalNotification<Object, Object> objectObjectRemovalNotification) {
        Object key = objectObjectRemovalNotification.getKey();
        Object value = objectObjectRemovalNotification.getValue();
        if (key == null) key = "";
        if (value == null) value = "";
        delete(key);
        delete(value);
        if (key instanceof CompletableFuture<?> future) {
            if (future.isDone()) try {
                delete(future.get());
            } catch (Exception ignored) {
            }
            else future.thenAccept(GenericCache::delete);
        }
        if (value instanceof CompletableFuture<?> future) {
            if (future.isDone()) try {
                delete(future.get());
            } catch (Exception ignored) {
            }
            else future.thenAccept(GenericCache::delete);
        }
    }

    private static void delete(Object obj) {
        try {
            if (obj instanceof AutoCloseable a) a.close();
        } catch (Exception e) {
            LOGGER.error("Failed to close value {}", obj);
        }
    }

    public FutureState<V> get(K key) {
        return FutureState.from(getOrLoad(key));
    }

    public CompletableFuture<V> getOrLoad(K key) {
        return cache.getUnchecked(key);
    }

    private CompletableFuture<byte[]> downloadBytes(K key) {
        Downloader downloader = type.getDownloader(key);
        Path path = type.getCacheFile(key, root);

        return downloadBytes(path, key, downloader, Util.nonCriticalIoPool());
    }

    private CompletableFuture<byte[]> downloadBytes(Path path, K key, Downloader downloader, Executor executor) {
        if (Files.isRegularFile(path)) {
            return CompletableFuture.supplyAsync(() -> {
                LOGGER.debug("Loading HTTP texture from local cache ({})", path);
                try (InputStream stream = Files.newInputStream(path)) {
                    return stream.readAllBytes();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, executor);
        }

        return downloader.download(executor).thenApply(data -> {
            try {
                FileUtil.createDirectoriesSafe(path.getParent());
                Files.write(path, data);
            } catch (IOException e) {
                LOGGER.info("Failed to cache key {} in {}", key, path);
            }
            return data;
        });
    }
}
