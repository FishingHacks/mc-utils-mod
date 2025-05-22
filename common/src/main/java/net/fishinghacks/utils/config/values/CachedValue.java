package net.fishinghacks.utils.config.values;

import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.platform.services.ConfigValue;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CachedValue<T> extends AbstractCachedValue<T> {
    private final Config config;
    private final ConfigValue<T> internalValue;

    private CachedValue(Config config, ConfigValue<T> internalValue, ConfigBuilder builder) {
        super(config, builder);
        this.config = config;
        this.internalValue = internalValue;
        finish(builder);
    }

    @Override
    protected T doGet() {
        return config.isLoaded() ? internalValue.get() : internalValue.getDefault();
    }

    @Override
    public T getRaw() {
        return internalValue.getRaw();
    }

    @Override
    public T getDefault() {
        return internalValue.getDefault();
    }

    @Override
    public boolean isValid(Object value) {
        return internalValue.isValid(value);
    }

    @Override
    protected void doSet(T value, boolean save) {
        internalValue.set(value);
        if (save) config.save();
    }

    @Override
    protected void doClearCache() {
        internalValue.clearCache();
    }

    @Override
    protected List<String> getPath() {
        return internalValue.getPath();
    }

    @Override
    protected String getTranslationKey() {
        return internalValue.getTranslation();
    }

    @Override
    public String getKey() {
        return internalValue.getKey();
    }

    public static <T> CachedValue<T> wrap(Config cfg, ConfigBuilder builder, String key, T defaultValue,
                                          Predicate<Object> validator) {
        return new CachedValue<>(cfg, builder.inner().define(key, defaultValue, validator), builder);
    }

    public static <T> CachedValue<T> wrap(Config cfg, ConfigBuilder builder, String key, T defaultValue) {
        return new CachedValue<>(cfg, builder.inner().define(key, defaultValue), builder);
    }

    public static <T extends Enum<T>> CachedValue<T> wrapEnum(Config cfg, ConfigBuilder builder, String key,
                                                              T defaultValue) {
        return new CachedValue<>(cfg, builder.inner().defineEnum(key, defaultValue), builder);
    }

    public static <T> CachedValue<List<? extends T>> wrapListEmpty(Config cfg, ConfigBuilder builder, String key,
                                                                   List<T> defaultValue, Supplier<T> defaultElement,
                                                                   Predicate<Object> itemValidator) {
        return new CachedValue<>(cfg,
            builder.inner().defineListAllowEmpty(key, defaultValue, defaultElement, itemValidator), builder);
    }

    public static <T> CachedValue<List<? extends T>> wrapListEmpty(Config cfg, ConfigBuilder builder, String key,
                                                                   Supplier<T> defaultElement,
                                                                   Predicate<Object> itemValidator) {
        return wrapListEmpty(cfg, builder, key, List.of(), defaultElement, itemValidator);
    }
}
