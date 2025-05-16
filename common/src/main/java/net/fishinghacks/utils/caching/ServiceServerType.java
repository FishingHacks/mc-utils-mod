package net.fishinghacks.utils.caching;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.connection.packets.CosmeticType;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ServiceServerType implements CacheType<String, NativeImage> {
    private final CosmeticType type;

    public ServiceServerType(CosmeticType type) {
        this.type = type;
    }

    @Override
    public String getFolderName() {
        return "service_server." + type.cacheDirectory();
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
        return new ServiceServerDownloader(key, type);
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
