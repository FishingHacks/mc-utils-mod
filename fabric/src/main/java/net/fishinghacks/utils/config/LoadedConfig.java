package net.fishinghacks.utils.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;

import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public record LoadedConfig(Config config, Path file, Lock lock) {
    public LoadedConfig(Config config, Path file) {
        this(config, file, new ReentrantLock());
    }

    public void save() {
        new TomlWriter().write(config, file, WritingMode.REPLACE_ATOMIC);

        lock.lock();
        var cfg = ConfigsImpl.KNOWN_CONFIGS.get(file);
        if (cfg != null) cfg.config().clearCache();
        lock.unlock();
    }
}
