package net.fishinghacks.utils.config.values;

import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.platform.services.ConfigValue;

public class ColorValue extends CachedMappedValue<Color, Integer> {
    protected ColorValue(Config config, ConfigValue<Integer> internalValue, ConfigBuilder builder) {
        super(config, internalValue, Color::argb, Color::fromARGB, builder);
    }
}
