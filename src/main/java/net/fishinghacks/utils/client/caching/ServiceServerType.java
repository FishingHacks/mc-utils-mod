package net.fishinghacks.utils.client.caching;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ServiceServerType implements CacheType<String, NativeImage> {
    public static final ServiceServerType instance = new ServiceServerType();

    @Override
    public String getFolderName() {
        return "service_server";
    }

    @Override
    public CompletableFuture<NativeImage> process(byte[] bytes) {
        try {
            return CompletableFuture.completedFuture(NativeImage.read(bytes));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public Downloader getDownloader(String key) {
        return new ServiceServerDownloader(key);
    }

    @Override
    public Path getCacheFile(String key, Path cacheDirectory) {
        String hash = Hashing.sha256().hashUnencodedChars(key).toString();
        return cacheDirectory.resolve(hash.length() > 2 ? hash.substring(0, 2) : "xx").resolve(escapeString(key));
    }

    private String escapeString(String key) {
        StringBuilder builder = new StringBuilder();
        for (char c : key.toCharArray()) {
            if (c == '_' || c == '-') builder.append(c);
            if (c >= 'a' && c <= 'z') builder.append(c);
            if (c >= 'A' && c <= 'Z') builder.append(c);
            if (c >= '0' && c <= '9') builder.append(c);
        }
        return builder.toString();
    }
}
