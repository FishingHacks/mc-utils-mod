package net.fishinghacks.utils.client.caching;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface Downloader {
    CompletableFuture<byte[]> download(Executor executor);
}
