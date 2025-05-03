package net.fishinghacks.utils.client.caching;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record HTTPDownloader(String url) implements Downloader {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public CompletableFuture<byte[]> download(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            LOGGER.info("Downloading HTTP file from {}", url);
            URI uri = URI.create(url);

            try {
                connection = (HttpURLConnection) uri.toURL().openConnection(Minecraft.getInstance().getProxy());
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode / 100 != 2)
                    throw new IOException("Failed to open " + uri + ", HTTP Error Code: " + responseCode);
                return connection.getInputStream().readAllBytes();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, executor);
    }
}
