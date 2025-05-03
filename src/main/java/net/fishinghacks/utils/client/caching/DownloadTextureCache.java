package net.fishinghacks.utils.client.caching;

import com.mojang.blaze3d.platform.NativeImage;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;

import java.util.EnumMap;

public class DownloadTextureCache extends GenericCache<String, NativeImage> {
    private static final EnumMap<TextureCacheType, DownloadTextureCache> caches = new EnumMap<>(TextureCacheType.class);
    public static DownloadTextureCache capeGallery;
    public static DownloadTextureCache optifine;
    public static DownloadTextureCache minecraftCapes;
    public static DownloadTextureCache serviceServer;

    public static void loadCaches(ClientStartedEvent ignored) {
        for(TextureCacheType type : TextureCacheType.values())
            caches.put(type, new DownloadTextureCache(type));
        capeGallery = caches.get(TextureCacheType.CapesGallery);
        optifine = caches.get(TextureCacheType.Optifine);
        minecraftCapes = caches.get(TextureCacheType.MinecraftCapes);
        serviceServer = new DownloadTextureCache(ServiceServerType.instance);
    }

    private DownloadTextureCache(CacheType<String, NativeImage> type) {
        super(type);
    }
}
