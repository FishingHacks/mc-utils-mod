package net.fishinghacks.utils.config.values;

import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.platform.services.ConfigValue;

public class CachedColorValue extends CachedMappedValue<Color, Integer> {
    protected CachedColorValue(AbstractConfig config, ConfigValue<Integer> internalValue, ConfigBuilder builder) {
        super(config, internalValue, Color::argb, Color::fromARGB, builder);
    }

    public static CachedColorValue wrap(AbstractConfig config, ConfigBuilder builder, String key, Color defaultValue) {
        return new CachedColorValue(config, builder.inner().define(key, defaultValue.argb()), builder);
    }
}
