package net.fishinghacks.utils.client.caching;

import net.fishinghacks.utils.client.connection.ClientConnectionHandler;
import net.fishinghacks.utils.common.connection.packets.Packets;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ServiceServerDownloader implements Downloader {
    private final String capename;

    public ServiceServerDownloader(String capename) {
        this.capename = capename;
    }

    @Override
    public CompletableFuture<byte[]> download(Executor ignored) {
        var inst = ClientConnectionHandler.getInstance();
        if (!inst.isConnected()) return CompletableFuture.failedFuture(new IOException("No connection"));

        CompletableFuture<byte[]> future = new CompletableFuture<>();
        inst.waitForPacket(Packets.COSMETIC_REPLY, packet -> {
            if(packet.isEmpty()) future.completeExceptionally(new IOException("connection closed early"));
            else if (packet.get().b64Data().isEmpty()) future.completeExceptionally(new IOException("no such texture"));
            else future.complete(Base64.decodeBase64(packet.get().b64Data()));
        }, packet -> capename.equals(packet.name()));
        return future;
    }
}
