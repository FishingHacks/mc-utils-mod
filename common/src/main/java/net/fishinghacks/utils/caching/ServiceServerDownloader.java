package net.fishinghacks.utils.caching;

import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.connection.packets.CosmeticRequestPacket;
import net.fishinghacks.utils.connection.packets.CosmeticType;
import net.fishinghacks.utils.connection.packets.Packets;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ServiceServerDownloader implements Downloader {
    private final String name;
    private final CosmeticType type;

    public ServiceServerDownloader(String name, CosmeticType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public CompletableFuture<byte[]> download(Executor ignored) {
        var inst = ClientConnectionHandler.getInstance();
        var conn = inst.getConnection();
        if (!inst.isConnected() || conn == null)
            return CompletableFuture.failedFuture(new IOException("No connection"));

        CompletableFuture<byte[]> future = new CompletableFuture<>();
        inst.waitForPacket(Packets.COSMETIC_REPLY, packet -> {
            if (packet.isEmpty()) future.completeExceptionally(new IOException("connection closed early"));
            else if (packet.get().b64Data().isEmpty()) future.completeExceptionally(new IOException("no such texture"));
            else future.complete(Base64.decodeBase64(packet.get().b64Data()));
        }, packet -> packet.cosmeticType() == type && name.equals(packet.name()));
        conn.send(new CosmeticRequestPacket(type, name));
        return future;
    }
}
