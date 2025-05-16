package net.fishinghacks.utils.gui.cosmetics;

import net.fishinghacks.utils.caching.DownloadTextureCache;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.connection.packets.CosmeticType;
import net.fishinghacks.utils.connection.packets.CosmeticsListRequestPacket;
import net.fishinghacks.utils.connection.packets.Packets;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerCosmetics implements Fetcher {
    private final int currentPage;
    private final boolean hasNext;
    private final List<CosmeticsEntry> entries;

    public static CompletableFuture<Fetcher> fetchCapes(int page) {
        var conn = ClientConnectionHandler.getInstance().getConnection();
        if (conn == null || !ClientConnectionHandler.getInstance().isConnected())
            return CompletableFuture.failedFuture(new Exception("No Connection"));
        var future = new CompletableFuture<Fetcher>();
        ClientConnectionHandler.getInstance().waitForPacket(Packets.LIST_CAPES, packet -> {
            if (packet.isEmpty()) {
                future.completeExceptionally(new Exception("Lost Connection"));
                return;
            }
            var capes = packet.get().capes();
            var pagedCapes = capes.subList(Math.min(page * 20 - 20, capes.size()), Math.min(page * 20, capes.size()));
            future.complete(new ServerCosmetics(page, capes.size() > page * 20, pagedCapes.stream().map(
                    titleAndHash -> new CosmeticsEntry(titleAndHash, titleAndHash,
                        DownloadTextureCache.serviceServerCapes))
                .toList()));
        });
        conn.send(new CosmeticsListRequestPacket(CosmeticType.Cape));
        return future;
    }

    public static CompletableFuture<Fetcher> fetchModels(int page) {
        var conn = ClientConnectionHandler.getInstance().getConnection();
        if (conn == null || !ClientConnectionHandler.getInstance().isConnected())
            return CompletableFuture.failedFuture(new Exception("No Connection"));
        var future = new CompletableFuture<Fetcher>();
        ClientConnectionHandler.getInstance().waitForPacket(Packets.LIST_MODELS, packet -> {
            if (packet.isEmpty()) {
                future.completeExceptionally(new Exception("Lost Connection"));
                return;
            }
            var models = packet.get().models();
            var pagedModels = models.subList(Math.min(page * 20 - 20, models.size()),
                Math.min(page * 20, models.size()));
            future.complete(new ServerCosmetics(page, models.size() > page * 20, pagedModels.stream().map(
                titleAndHash -> new CosmeticsEntry(titleAndHash, titleAndHash,
                    DownloadTextureCache.serviceServerModelsPreview)).toList()));
        });
        conn.send(new CosmeticsListRequestPacket(CosmeticType.ModelPreview));
        return future;
    }

    private ServerCosmetics(int currentPage, boolean hasNext, List<CosmeticsEntry> entries) {
        this.currentPage = currentPage;
        this.hasNext = hasNext;
        this.entries = entries;
    }

    @Override
    public int currentPage() {
        return currentPage;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public boolean hasPrev() {
        return currentPage > 1;
    }

    @Override
    public List<CosmeticsEntry> getFetched() {
        return entries;
    }
}
