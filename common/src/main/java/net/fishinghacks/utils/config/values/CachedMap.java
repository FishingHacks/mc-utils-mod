package net.fishinghacks.utils.config.values;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CachedMap<K, V> extends CachedMappedValue<HashMap<K, V>, List<? extends Config>> {
    public static Config configMap(Map<String, Object> map) {
        return InMemoryFormat.defaultInstance().createConfig(() -> map);
    }

    public static <K, V> Config configMap(Stream<Map.Entry<K, V>> stream, Function<K, String> mapKey,
                                          Function<V, Object> mapValue) {
        return InMemoryFormat.defaultInstance().createConfig(
            () -> stream.collect(Collectors.toMap(v -> mapKey.apply(v.getKey()), v -> mapValue.apply(v.getValue()))));
    }

    public static <K, V> Config configMap(Stream<Map.Entry<K, V>> stream, Function<V, Object> mapValue) {
        return InMemoryFormat.defaultInstance().createConfig(
            () -> stream.collect(Collectors.toMap(v -> v.getKey().toString(), v -> mapValue.apply(v.getValue()))));
    }

    protected CachedMap(AbstractConfig config, String key, Function<HashMap<K, V>, Config> encode,
                        BiConsumer<Config, HashMap<K, V>> decode, ConfigBuilder builder) {
        super(config, builder.inner()
            .defineListAllowEmpty(key, List.of(), () -> InMemoryFormat.defaultInstance().createConfig(),
                obj -> obj instanceof Config), v -> List.of(encode.apply(v)), new Decoder<>(decode), builder);
    }

    record Decoder<K, V>(
        BiConsumer<Config, HashMap<K, V>> decode) implements Function<List<? extends Config>, HashMap<K, V>> {
        @Override
        public HashMap<K, V> apply(List<? extends Config> configs) {
            var map = new HashMap<K, V>();
            for (var config : configs) decode.accept(config, map);
            return map;
        }
    }
}
