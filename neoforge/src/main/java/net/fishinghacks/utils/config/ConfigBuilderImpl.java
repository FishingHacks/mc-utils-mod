package net.fishinghacks.utils.config;

import net.fishinghacks.utils.platform.services.ConfigValue;
import net.fishinghacks.utils.platform.services.IConfig;
import net.fishinghacks.utils.platform.services.IConfigBuilder;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record ConfigBuilderImpl(ModConfigSpec.Builder builder) implements IConfigBuilder {
    public ConfigBuilderImpl() {
        this(new ModConfigSpec.Builder());
    }

    @Override
    public IConfigBuilder translation(String key) {
        builder.translation(key);
        return this;
    }

    @Override
    public <T> ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
        return new ConfigValueImpl<>(builder.define(path, defaultValue, validator));
    }

    @Override
    public ConfigValue<Boolean> define(String path, boolean defaultValue) {
        return new ConfigValueImpl<>(builder.define(path, defaultValue));
    }

    @Override
    public <T> ConfigValue<T> define(String path, T defaultValue) {
        return new ConfigValueImpl<>(builder.define(path, defaultValue));
    }

    @Override
    public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<T> defaultValue,
                                                                   Supplier<T> createElement,
                                                                   Predicate<Object> validator) {
        return new ConfigValueImpl<>(builder.defineListAllowEmpty(path, defaultValue, createElement, validator));
    }

    @Override
    public IConfigBuilder worldRestart() {
        builder.worldRestart();
        return this;
    }

    @Override
    public IConfigBuilder gameRestart() {
        builder.gameRestart();
        return this;
    }

    @Override
    public IConfigBuilder enterSection(String name) {
        builder.push(name);
        return this;
    }

    @Override
    public IConfigBuilder exitSection() {
        builder.pop();
        return this;
    }

    @Override
    public IConfig build() {
        return new ConfigImpl(builder.build());
    }
}
