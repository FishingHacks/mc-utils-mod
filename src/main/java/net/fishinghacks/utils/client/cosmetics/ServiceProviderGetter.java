package net.fishinghacks.utils.client.cosmetics;

import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.client.caching.DownloadTextureCache;
import net.fishinghacks.utils.client.connection.ClientConnectionHandler;
import net.fishinghacks.utils.common.connection.packets.GetCosmeticForPlayer;
import net.fishinghacks.utils.common.connection.packets.GetCosmeticForPlayerReply;
import net.fishinghacks.utils.common.connection.packets.Packets;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServiceProviderGetter {
    @NotNull
    public static CompletableFuture<NativeImage> get(UUID id) {
        var conn = ClientConnectionHandler.getInstance().getConnection();
        if(conn == null || !conn.isConnected()) return CompletableFuture.failedFuture(new Exception("No connection"));
        CompletableFuture<GetCosmeticForPlayerReply> future = new CompletableFuture<>();
        ClientConnectionHandler.getInstance().waitForPacket(Packets.GET_PLAYER_COSMETIC_REPLY, packet -> {
            if(packet.isEmpty()) future.completeExceptionally(new Exception("connection closed"));
            else future.complete(packet.get());
        }, packet -> packet.player().equals(id));
        conn.send(new GetCosmeticForPlayer(id));
        return future.thenCompose(packet -> {
            if(packet.cosmeticName() == null) throw new RuntimeException("Player does not have a cosmetic");
            if(packet.isMCCapes()) return DownloadTextureCache.capeGallery.getOrLoad(packet.cosmeticName());
            else return DownloadTextureCache.serviceServer.getOrLoad(packet.cosmeticName());
        });
    }
}
