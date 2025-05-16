package net.fishinghacks.utils.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fishinghacks.utils.platform.services.ConfigValue;

import javax.annotation.Nullable;
import java.util.*;

public class CosmeticMapConfigValue {
    private final ConfigValue<String> internalValue;
    private final Gson gson = new Gson();
    private final Map<UUID, PlayerCosmetics> value = new HashMap<>();
    private final Config config;

    public CosmeticMapConfigValue(ConfigValue<String> internalValue, Config config) {
        this.internalValue = internalValue;
        this.config = config;
        config.addCachedValue(this::clearCache);
    }

    public Map<UUID, PlayerCosmetics> getValue() {
        return value;
    }

    public void updated() {
        internalValue.set(gson.toJson(value));
        config.save();
    }

    public void clearCache() {
        value.clear();
        var map = gson.fromJson(internalValue.get(), JsonObject.class);
        var adapter = gson.getAdapter(PlayerCosmetics.class);
        for (var k : map.keySet()) {
            try {
                UUID uuid = UUID.fromString(k);
                var cosmetics = adapter.fromJsonTree(map.get(k));
                if (cosmetics != null) value.put(uuid, cosmetics);
            } catch (IllegalArgumentException ignored) {
            }
        }
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