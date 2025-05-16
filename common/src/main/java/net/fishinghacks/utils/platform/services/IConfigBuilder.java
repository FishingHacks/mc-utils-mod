package net.fishinghacks.utils.platform.services;

import net.fishinghacks.utils.Translation;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface IConfigBuilder {
    IConfigBuilder translation(String key);

    default IConfigBuilder translation(Translation translation) {
        return translation(translation.key());
    }

    <T> ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator);

    ConfigValue<Boolean> define(String path, boolean defaultValue);
    <T> ConfigValue<T> define(String path, T defaultValue);

    <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<T> defaultValue, Supplier<T> createElement,
                                            Predicate<Object> validator);

    IConfigBuilder worldRestart();

    IConfigBuilder gameRestart();

    IConfigBuilder enterSection(String name);

    IConfigBuilder exitSection();

    public IConfig build();
}
