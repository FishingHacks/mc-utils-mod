package net.fishinghacks.utils.common.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmeticMapConfigValue {
    private final ModConfigSpec.ConfigValue<String> internalValue;
    private final Gson gson = new Gson();
    private final Map<UUID, PlayerCosmetics> value = new HashMap<>();
    private final Config config;

    public CosmeticMapConfigValue(ModConfigSpec.ConfigValue<String> internalValue, Config config) {
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

        public PlayerCosmetics updateCape(@Nullable String cape, boolean capeIsMCCapes) {
            this.cape = cape;
            this.capeIsMCCapes = capeIsMCCapes;
            return this;
        }
    }
}