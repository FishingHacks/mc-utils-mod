package net.fishinghacks.utils.config.values;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.platform.services.ConfigValue;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class CosmeticMapConfigValue extends CachedMappedValue<Map<UUID, CosmeticMapConfigValue.PlayerCosmetics>,
    String> {
    protected CosmeticMapConfigValue(Config config, ConfigValue<String> internalValue,
                                     Function<Map<UUID, PlayerCosmetics>, String> encode,
                                     Function<String, Map<UUID, PlayerCosmetics>> decode, ConfigBuilder builder) {
        super(config, internalValue, encode, decode, builder);
    }

    public static CosmeticMapConfigValue wrap(Config config, ConfigBuilder builder, String key, String defaultValue) {
        var gson = new Gson();
        return new CosmeticMapConfigValue(config, builder.inner().define(key, defaultValue), gson::toJson,
            s -> parse(s, gson), builder);
    }

    private static Map<UUID, PlayerCosmetics> parse(String string, Gson gson) {
        var object = gson.fromJson(string, JsonObject.class);
        var adapter = gson.getAdapter(PlayerCosmetics.class);
        var map = new HashMap<UUID, PlayerCosmetics>();
        for (var k : object.entrySet()) {
            try {
                UUID uuid = UUID.fromString(k.getKey());
                var cosmetics = adapter.fromJsonTree(k.getValue());
                if (cosmetics != null) map.put(uuid, cosmetics);
            } catch (Exception e) {
                Constants.LOG.info("Failed to parse cosmetics for player {} ({})", k.getKey(), k.getValue(), e);
            }
        }
        return map;
    }

    public static class PlayerCosmetics {
        public @Nullable String cape;
        public boolean capeIsMCCapes = false;
        public HashSet<String> models = new HashSet<>();

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