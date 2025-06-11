package net.fishinghacks.utils.config.values;

import com.electronwill.nightconfig.core.Config;
import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class CosmeticMapConfigValue extends CachedMap<UUID, CosmeticMapConfigValue.PlayerCosmetics> {
    protected CosmeticMapConfigValue(AbstractConfig config, String key,
                                     Function<HashMap<UUID, PlayerCosmetics>, Config> encode,
                                     BiConsumer<Config, HashMap<UUID, PlayerCosmetics>> decode, ConfigBuilder builder) {
        super(config, key, encode, decode, builder);
    }

    private static Config encode(HashMap<UUID, PlayerCosmetics> value) {
        return configMap(value.entrySet().stream(), PlayerCosmetics::encode);
    }

    private static void decode(Config cfg, HashMap<UUID, PlayerCosmetics> value) {
        for (var entry : cfg.entrySet()) {
            try {
                var id = UUID.fromString(entry.getKey());
                if (!(entry instanceof Config c)) continue;
                value.put(id, PlayerCosmetics.decode(c));
            } catch (Exception ignored) {
            }
        }
    }

    public static CosmeticMapConfigValue wrap(AbstractConfig config, ConfigBuilder builder, String key) {
        return new CosmeticMapConfigValue(config, key, CosmeticMapConfigValue::encode, CosmeticMapConfigValue::decode,
            builder);
    }

    public static class PlayerCosmetics {
        public @Nullable String cape;
        public boolean capeIsMCCapes = false;
        public HashSet<String> models = new HashSet<>();

        public static PlayerCosmetics decode(Config cfg) {
            var cape = cfg.get("cape");
            var capeIsMCCapes = cfg.get("capeIsMCCapes");
            var models = cfg.get("models");
            if (!(capeIsMCCapes instanceof Boolean) || !(cape instanceof String)) {
                cape = null;
                capeIsMCCapes = false;
            }
            var modelSet = new HashSet<String>();
            if (models instanceof HashSet<?> set) for (var model : set)
                if (model instanceof String s) modelSet.add(s);

            return new PlayerCosmetics().updateCape((String) cape, (Boolean) capeIsMCCapes).setModels(modelSet);
        }

        public Config encode() {
            if (cape == null) return configMap(Map.of("capeIsMCCapes", capeIsMCCapes, "models", models));
            return configMap(Map.of("cape", cape, "capeIsMCCapes", capeIsMCCapes, "models", models));
        }

        public PlayerCosmetics updateCape(@Nullable String cape, boolean capeIsMCCapes) {
            this.cape = cape;
            this.capeIsMCCapes = capeIsMCCapes;
            return this;
        }

        public PlayerCosmetics addModel(String model) {
            models.add(model);
            return this;
        }

        public PlayerCosmetics removeModel(String model) {
            models.remove(model);
            return this;
        }

        public PlayerCosmetics setModels(HashSet<String> models) {
            this.models = models;
            return this;
        }

        public PlayerCosmetics setModels(List<String> models) {
            return setModels(new HashSet<>(models));
        }
    }
}