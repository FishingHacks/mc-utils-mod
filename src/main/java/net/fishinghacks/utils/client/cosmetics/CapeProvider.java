package net.fishinghacks.utils.client.cosmetics;


// https://api.minecraftcapes.net/api/gallery/get?page=2

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.client.caching.DownloadTextureCache;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public enum CapeProvider {
    ServiceProvider("Service Server"), Builtin("Builtin"), Optifine("OptiFine"), MinecraftCapes("MinecraftCapes");

    public static final CapeProvider first = ServiceProvider;

    public final String name;

    CapeProvider(String name) {
        this.name = name;
    }

    @Nullable
    public CapeProvider next() {
        return switch (this) {
            case ServiceProvider -> Builtin;
            case Builtin -> Optifine;
            case Optifine -> MinecraftCapes;
            case MinecraftCapes -> null;
        };
    }

    @NotNull
    public CompletableFuture<NativeImage> get(GameProfile profile) {
        return switch (this) {
            case ServiceProvider -> ServiceProviderGetter.get(profile.getId());
            case Builtin -> {
                if (Objects.equals(profile.getId().toString(),
                    "2a312138-2b30-4a6c-b43d-784d0d755e44") || profile.getName().equals("Dev")) {
                    yield DownloadTextureCache.capeGallery.getOrLoad(
                        "014fe5e6a44df115dfeeb631cd4ccce0843de3f5beaad15f7f98d31af7e6ef94");
                }
                yield CompletableFuture.failedFuture(new Exception("No builtin cape found"));
            }
            case Optifine -> DownloadTextureCache.capeGallery.getOrLoad(profile.getName());
            case MinecraftCapes -> MinecraftCapesGetter.get(profile.getId().toString().replaceAll("-", ""));
        };
    }
}
