package net.fishinghacks.utils.config.values;

import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.platform.services.ConfigValue;

public class ColorValue extends CachedMappedValue<Color, Integer> {
    protected ColorValue(Config config, ConfigValue<Integer> internalValue, ConfigBuilder builder) {
        super(config, internalValue, Color::argb, Color::fromARGB, builder);
    }

    public static ColorValue wrap(Config config, ConfigBuilder builder, String key, Color defaultValue) {
        return new ColorValue(config, builder.inner().define(key, defaultValue.argb()), builder);
    }
}
