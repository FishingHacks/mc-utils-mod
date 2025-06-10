package net.fishinghacks.utils.config.values;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.platform.services.ConfigValue;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.function.Function;

public class MufflerState extends CachedMappedValue<HashMap<ResourceLocation, Integer>, String> {
    protected MufflerState(Config config, ConfigValue<String> internalValue,
                           Function<HashMap<ResourceLocation, Integer>, String> encode,
                           Function<String, HashMap<ResourceLocation, Integer>> decode, ConfigBuilder builder) {
        super(config, internalValue, encode, decode, builder);
    }

    protected static String encode(HashMap<ResourceLocation, Integer> value) {
        var object = new JsonObject();
        for (var entry : value.entrySet()) {
            if (entry.getValue() == 100) continue;
            object.add(entry.getKey().toString(), new JsonPrimitive(entry.getValue()));
        }
        return new Gson().toJson(value);
    }

    protected static HashMap<ResourceLocation, Integer> decode(String value) {
        var map = new HashMap<ResourceLocation, Integer>();
        var object = new Gson().fromJson(value, JsonObject.class);
        for (var entry : object.entrySet()) {
            try {
                var v = entry.getValue().getAsInt();
                if(v == 100) continue;
                var k = ResourceLocation.parse(entry.getKey());
                map.put(k, v);
            } catch (Exception ignored) {
            }

        }
        return map;
    }

    public static MufflerState wrap(Config config, ConfigBuilder builder, String key) {
        return new MufflerState(config, builder.inner().define(key, "{}"), MufflerState::encode, MufflerState::decode,
            builder);
    }
}
