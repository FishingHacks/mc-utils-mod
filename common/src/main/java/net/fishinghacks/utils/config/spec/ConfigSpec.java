package net.fishinghacks.utils.config.spec;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.config.values.AbstractCachedValue;
import net.fishinghacks.utils.gui.configuration.TranslationChecker;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class ConfigSpec {
    public final LinkedHashMap<String, SpecHolder> elements = new LinkedHashMap<>();
    public final List<String> path;

    public String getTranslationKey() {
        StringBuilder fullString = new StringBuilder(Constants.MOD_ID + ".configuration");
        for (var entry : path) {
            fullString.append('.');
            fullString.append(entry);
        }
        return TranslationChecker.getWithFallback(fullString.toString(),
            Constants.MOD_ID + ".configuration." + path.getLast());

    }

    public ConfigSpec(List<String> path) {
        this.path = path;
    }

    void addValue(AbstractCachedValue<?> value) {
        elements.put(value.getKey(), new SpecHolder(value));
    }

    ConfigSpec createSubconfig(String key) {
        var list = new ArrayList<>(path);
        list.add(key);
        ConfigSpec spec = new ConfigSpec(list);
        elements.put(key, new SpecHolder(spec));
        return spec;
    }

    /// Note: `value` should only ever be `AbstractCachedValue<?>` or `ConfigSpec`.
    public static final class SpecHolder {
        @NotNull
        private final Object value;

        public SpecHolder(ConfigSpec spec) {
            this((Object) spec);
        }

        public SpecHolder(AbstractCachedValue<?> value) {
            this((Object) value);
        }

        private SpecHolder(@NotNull Object value) {
            this.value = value;
        }

        public boolean isValue() {
            return value instanceof AbstractCachedValue<?>;
        }

        public boolean isSubconfig() {
            return value instanceof ConfigSpec;
        }

        public void match(Consumer<ConfigSpec> whenSpec, Consumer<AbstractCachedValue<?>> whenValue) {
            if (value instanceof ConfigSpec spec) whenSpec.accept(spec);
            else whenValue.accept((AbstractCachedValue<?>) value);
        }

        public ConfigSpec asSpec() {
            return (ConfigSpec) value;
        }

        public AbstractCachedValue<?> asCachedValue() {
            return (AbstractCachedValue<?>) value;
        }
    }
}
