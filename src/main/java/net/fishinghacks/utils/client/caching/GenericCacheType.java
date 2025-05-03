package net.fishinghacks.utils.client.caching;

import it.unimi.dsi.fastutil.Function;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class GenericCacheType<K, V> implements CacheType<K, V> {
    private final String folderName;
    private final Function<K, Downloader> downloaderProvider;
    private final BiFunction<K, Path, Path> cacheFile;
    private final Function<byte[], CompletableFuture<V>> processor;

    public GenericCacheType(String folderName, Function<K, Downloader> downloaderProvider,
                            BiFunction<K, Path, Path> cacheFile, Function<byte[], CompletableFuture<V>> processor) {
        this.folderName = folderName;
        this.downloaderProvider = downloaderProvider;
        this.cacheFile = cacheFile;
        this.processor = processor;
    }

    public static <K, V> GenericCacheType<K, V> of(String folderName, Function<K, Downloader> downloaderProvider,
                                                   BiFunction<K, Path, Path> cacheFile, Function<byte[], V> processor) {
        return new GenericCacheType<>(folderName, downloaderProvider, cacheFile,
            (bytes) -> CompletableFuture.supplyAsync(() -> processor.apply((byte[]) bytes)));
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

    @Override
    public Path getCacheFile(K key, Path cacheDirectory) {
        return cacheFile.apply(key, cacheDirectory);
    }


    @Override
    public CompletableFuture<V> process(byte[] bytes) {
        return processor.apply(bytes);
    }

    @Override
    public Downloader getDownloader(K key) {
        return downloaderProvider.apply(key);
    }
}
