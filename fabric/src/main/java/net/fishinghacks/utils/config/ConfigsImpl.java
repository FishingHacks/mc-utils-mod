package net.fishinghacks.utils.config;

import com.electronwill.nightconfig.core.concurrent.ConcurrentConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigType;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ConfigsImpl {
    static final Map<Path, ConfigHolder> KNOWN_CONFIGS = new HashMap<>();

    public static void registerConfigs(Path configDir, Predicate<ConfigType> shouldLoad) {
        Configs.register(null, (ignored, config) -> registerConfig(configDir, config, shouldLoad));
    }

    public static void registerConfig(Path configDir, Config config, Predicate<ConfigType> shouldLoad) {
        String filename = config.getFilename();
        if (filename == null) filename = Constants.MOD_ID + "-" + config.type().extension() + ".toml";
        Path file = configDir.resolve(filename);
        ConfigImpl spec = (ConfigImpl) config.getConfig();
        KNOWN_CONFIGS.put(file, new ConfigHolder(config, spec));
        if (!shouldLoad.test(config.type())) return;

        openConfig(file);
    }

    public static void openConfig(Path path) {
        var configHolder = KNOWN_CONFIGS.get(path);
        if (configHolder == null) throw new IllegalStateException("no config registered under path " + path);
        if (configHolder.impl.isLoaded()) throw new IllegalStateException("cannot open already loaded config " + path);
        loadConfig(path, configHolder.impl);
    }

    public static void reload() {
        Constants.LOG.info("Reloading!! {}", KNOWN_CONFIGS);
        for (var entry : KNOWN_CONFIGS.entrySet()) {
            if (!entry.getValue().impl.isLoaded()) return;
            Constants.LOG.info("Reloading {}", entry.getKey());
            loadConfig(entry.getKey(), entry.getValue().impl());
        }
    }

    public static void loadConfig(Path path, ConfigImpl impl) {
        try {
            impl.load(new LoadedConfig(readConfig(path), path));
        } catch (NoSuchFileException ignored) {
            try {
                setupConfig(path, impl);
                impl.load(new LoadedConfig(readConfig(path), path));
            } catch (IOException ex) {
                throw new RuntimeException("Failed to create default config file " + path, ex);
            }
        } catch (IOException | ParsingException e) {
            Constants.LOG.warn("Failed to load config {}: {}, Attempting to recreate. A backup will be made.", path, e);
            try {
                backupConfig(path);
                Files.delete(path);
                setupConfig(path, impl);
                impl.load(new LoadedConfig(readConfig(path), path));
            } catch (Throwable t) {
                e.addSuppressed(t);

                throw new RuntimeException("Failed to recreate config file " + path, e);
            }
        }
    }

    private static void backupConfig(Path path) {
        Path bakFileLocation = path.getParent();
        String bakFileName = FilenameUtils.removeExtension(path.getFileName().toString());
        String bakFileExtension = FilenameUtils.getExtension(path.getFileName().toString()) + ".bak";
        Path bakFile = bakFileLocation.resolve(bakFileName + "-1" + "." + bakFileExtension);
        try {
            for (int i = 5; i > 0; i--) {
                Path oldBak = bakFileLocation.resolve(bakFileName + "-" + i + "." + bakFileExtension);
                if (Files.exists(oldBak)) {
                    if (i >= 5) Files.delete(oldBak);
                    else Files.move(oldBak,
                        bakFileLocation.resolve(bakFileName + "-" + (i + 1) + "." + bakFileExtension));
                }
            }
            Files.copy(path, bakFile);
        } catch (IOException exception) {
            Constants.LOG.warn("Failed to back up config file {}", path, exception);
        }

    }

    private static void setupConfig(Path path, ConfigImpl config) throws IOException {
        Files.createDirectories(path.getParent());
        new TomlWriter().write(config.createDefaultConfig(), path, WritingMode.REPLACE_ATOMIC);
    }

    private static ConcurrentConfig readConfig(Path path) throws IOException, ParsingException {
        try (var reader = Files.newBufferedReader(path)) {
            var config = new SynchronizedConfig(TomlFormat.instance(), LinkedHashMap::new);
            config.bulkUpdate(view -> {
                new TomlParser().parse(reader, view, ParsingMode.REPLACE);
            });
            return config;
        }
    }

    public record ConfigHolder(Config config, ConfigImpl impl) {
    }
}
