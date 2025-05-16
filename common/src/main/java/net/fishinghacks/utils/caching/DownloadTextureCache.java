package net.fishinghacks.utils.caching;

import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.connection.packets.CosmeticType;

import java.util.EnumMap;

public class DownloadTextureCache extends GenericCache<String, NativeImage> {
    private static final EnumMap<TextureCacheType, DownloadTextureCache> caches = new EnumMap<>(TextureCacheType.class);
    public static DownloadTextureCache capeGallery;
    public static DownloadTextureCache optifine;
    public static DownloadTextureCache minecraftCapes;
    public static DownloadTextureCache serviceServerCapes;
    public static DownloadTextureCache serviceServerModelsPreview;
    public static DownloadTextureCache serviceServerModels;

    public static void loadCaches() {
        Constants.LOG.info("Loaded caches");
        for (TextureCacheType type : TextureCacheType.values())
            caches.put(type, new DownloadTextureCache(type));
        capeGallery = caches.get(TextureCacheType.CapesGallery);
        optifine = caches.get(TextureCacheType.Optifine);
        minecraftCapes = caches.get(TextureCacheType.MinecraftCapes);
        serviceServerCapes = new DownloadTextureCache(new ServiceServerType(CosmeticType.Cape));
        serviceServerModels = new DownloadTextureCache(new ServiceServerType(CosmeticType.ModelTexture));
        serviceServerModelsPreview = new DownloadTextureCache(new ServiceServerType(CosmeticType.ModelPreview));
    }

    private DownloadTextureCache(CacheType<String, NativeImage> type) {
        super(type);
    }
}
