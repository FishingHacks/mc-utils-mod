package net.fishinghacks.utils.cosmetics;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.caching.HTTPDownloader;
import net.minecraft.Util;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MinecraftCapesGetter {
    public static CompletableFuture<NativeImage> get(String key) {
        return new HTTPDownloader("https://api.minecraftcapes.net/profile/" + key).download(Util.nonCriticalIoPool())
            .thenApply(bytes -> {
                try {
                    Data result = new Gson().fromJson(new InputStreamReader(new ByteArrayInputStream(bytes)),
                        Data.class);
                    var cape = result.textures.get("cape");
                    if (cape == null) throw new IOException("Failed to read cape");
                    return NativeImage.read(Base64.decodeBase64(cape));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }

    private record Data(Map<String, String> textures) {
    }
}
