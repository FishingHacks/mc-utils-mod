package net.fishinghacks.utils.caching;

import com.mojang.blaze3d.platform.NativeImage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public enum TextureCacheType implements CacheType<String, NativeImage> {
    CapesGallery("capes_gallery", "https://api.minecraftcapes.net/api/gallery/%s/preview/map"), Optifine(
        "optifine_capes", "http://s.optifine.net/capes/%s.png");

    public final String name;
    private final String urlFormat;

    TextureCacheType(String name, String urlFormat) {
        this.name = name;
        this.urlFormat = urlFormat;
    }

    @Override
    public String getFolderName() {
        return name;
    }

    @Override
    public CompletableFuture<NativeImage> process(byte[] bytes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return NativeImage.read(bytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public Downloader getDownloader(String key) {
        return new HTTPDownloader(String.format(urlFormat, key));
    }

    @Override
    public Path getCacheFile(String key, Path cacheDirectory) {
        return cacheDirectory.resolve(key.length() > 2 ? key.substring(0, 2) : "xx").resolve(key);
    }
}