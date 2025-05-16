package net.fishinghacks.utils.config;

import net.fishinghacks.utils.platform.services.ConfigValue;
import net.fishinghacks.utils.platform.services.IConfig;
import net.fishinghacks.utils.platform.services.IConfigBuilder;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl implements IConfigBuilder {
    @Override
    public IConfigBuilder translation(String key) {
        return this;
    }

    @Override
    public <T> ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
        return new ConfigValueImpl<>(defaultValue);
    }

    @Override
    public ConfigValue<Boolean> define(String path, boolean defaultValue) {
        return new ConfigValueImpl<>(defaultValue);
    }

    @Override
    public <T> ConfigValue<T> define(String path, T defaultValue) {
        return new ConfigValueImpl<>(defaultValue);
    }

    @Override
    public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<T> defaultValue,
                                                                   Supplier<T> createElement,
                                                                   Predicate<Object> validator) {
        return new ConfigValueImpl<>(defaultValue);
    }

    @Override
    public IConfigBuilder worldRestart() {
        return this;
    }

    @Override
    public IConfigBuilder gameRestart() {
        return this;
    }

    @Override
    public IConfigBuilder enterSection(String name) {
        return this;
    }

    @Override
    public IConfigBuilder exitSection() {
        return this;
    }

    @Override
    public IConfig build() {
        return new ConfigImpl();
    }
}
