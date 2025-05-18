package net.fishinghacks.utils.config.values;

import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.platform.services.ConfigValue;

import java.util.List;
import java.util.function.Function;

public class CachedMappedValue<T, U> extends AbstractCachedValue<T> {
    private final Config config;
    private final ConfigValue<U> internalValue;
    private final Function<T, U> encode;
    private final Function<U, T> decode;

    protected CachedMappedValue(Config config, ConfigValue<U> internalValue, Function<T, U> encode,
                                Function<U, T> decode, ConfigBuilder builder) {
        super(config, builder);
        this.config = config;
        this.internalValue = internalValue;
        this.encode = encode;
        this.decode = decode;
        finish(builder);
    }

    @Override
    protected T doGet() {
        return decode.apply(config.isLoaded() ? internalValue.get() : internalValue.getDefault());
    }

    @Override
    public T getRaw() {
        return decode.apply(internalValue.getRaw());
    }

    @Override
    public T getDefault() {
        return decode.apply(internalValue.getDefault());
    }

    @Override
    public boolean isValid(Object value) {
        return internalValue.isValid(value);
    }

    @Override
    protected void doSet(T value, boolean save) {
        internalValue.set(encode.apply(value));
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
}
