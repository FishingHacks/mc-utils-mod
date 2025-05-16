package net.fishinghacks.utils.caching;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface CacheType<K, V> {
    /// Gets the folder name of this type in the cache directory
    String getFolderName();
    /// Gets the path for the cached file, relative to the cache directory.
    Path getCacheFile(K key, Path cacheDirectory);
    /// Processes the bytes into the appropriate value
    CompletableFuture<V> process(byte[] bytes);
    /// Returns the url to download the key from
    Downloader getDownloader(K key);
}
