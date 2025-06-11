package net.fishinghacks.utils.config.values;

import com.electronwill.nightconfig.core.Config;
import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MufflerState extends CachedMap<ResourceLocation, Integer> {
    protected MufflerState(AbstractConfig config, String key,
                           Function<HashMap<ResourceLocation, Integer>, Config> encode,
                           BiConsumer<Config, HashMap<ResourceLocation, Integer>> decode, ConfigBuilder builder) {
        super(config, key, encode, decode, builder);
    }

    protected static Config encode(HashMap<ResourceLocation, Integer> value) {
        return configMap(value.entrySet().stream().filter(v -> v.getValue() != 100), ResourceLocation::toString,
            v -> v);
    }

    protected static void decode(Config cfg, HashMap<ResourceLocation, Integer> value) {
        for (var entry : cfg.entrySet()) {
            if (!(entry.getValue() instanceof Integer volume)) continue;
            var location = ResourceLocation.tryParse(entry.getKey());
            if (location == null) continue;
            value.put(location, Math.clamp(volume, -100, 100));
        }
    }

    public static MufflerState wrap(AbstractConfig config, ConfigBuilder builder, String key) {
        return new MufflerState(config, key, MufflerState::encode, MufflerState::decode, builder);
    }
}