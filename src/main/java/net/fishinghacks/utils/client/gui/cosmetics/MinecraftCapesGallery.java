package net.fishinghacks.utils.client.gui.cosmetics;

import com.google.gson.Gson;
import net.fishinghacks.utils.client.caching.DownloadTextureCache;
import net.fishinghacks.utils.client.caching.HTTPDownloader;
import net.minecraft.Util;
import org.apache.commons.lang3.exception.UncheckedException;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// url: https://api.minecraftcapes.net/api/gallery/get?page=N&type={"cape":true,"ears":false,"animated":true}
// encoded: https://api.minecraftcapes.net/api/gallery/get?page=N&type=%7B%22cape%22:true,%22ears%22:false,%22animated%22:true%7D

public class MinecraftCapesGallery implements Fetcher {
    public final List<Entry> data;
    public final boolean hasPrevPage;
    public final boolean hasNextPage;
    public final int current_page;

    public static CompletableFuture<Fetcher> fetchPage(int page) {
        if (page < 0) return CompletableFuture.completedFuture(new MinecraftCapesGallery(List.of(), false, true, -1));
        return new HTTPDownloader(
            "https://api.minecraftcapes.net/api/gallery/get?page=" + page + "&type=%7B%22cape%22:true,%22ears%22" +
                ":false,%22animated%22:true%7D").download(
            Util.nonCriticalIoPool()).thenApply(bytes -> {
            try {
                return new Gson().fromJson(new InputStreamReader(new ByteArrayInputStream(bytes)),
                    MinecraftCapesGallery.class);
            } catch (Exception e) {
                throw new UncheckedException(e);
            }
        });
    }

    @Override
    public int currentPage() {
        return current_page;
    }

    @Override
    public boolean hasNext() {
        return hasNextPage;
    }

    @Override
    public boolean hasPrev() {
        return hasPrevPage;
    }

    @Override
    public List<CosmeticsEntry> getFetched() {
        return this.data.stream().map(Entry::fetch).toList();
    }

    public MinecraftCapesGallery(List<Entry> data, boolean hasPrevPage, boolean hasNextPage, int currentPage) {
        this.data = data;
        this.hasPrevPage = hasPrevPage;
        this.hasNextPage = hasNextPage;
        current_page = currentPage;
    }

    public record Entry(String hash, String title) {
        public CosmeticsEntry fetch() {
            return new CosmeticsEntry(title, hash, DownloadTextureCache.capeGallery);
        }
    }

}
