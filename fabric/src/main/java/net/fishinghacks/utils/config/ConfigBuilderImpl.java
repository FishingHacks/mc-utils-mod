package net.fishinghacks.utils.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.fishinghacks.utils.platform.services.ConfigValue;
import net.fishinghacks.utils.platform.services.IConfig;
import net.fishinghacks.utils.platform.services.IConfigBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl implements IConfigBuilder {
    private static final Splitter pathSplitter = Splitter.on('.');

    private final Config spec = Config.of(LinkedHashMap::new, InMemoryFormat.withUniversalSupport());
    private BuilderContext context = new BuilderContext();
    private final List<String> currentPath = new ArrayList<>();
    final List<ConfigValueImpl<?>> values = new ArrayList<>();

    @Override
    public IConfigBuilder translation(String key) {
        context.currentTranslation = key;
        return this;
    }

    @Override
    public <T> ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
        Objects.requireNonNull(defaultValue, "Default value can not be null");
        context.clazz = Object.class;
        return define(Lists.newArrayList(pathSplitter.split(path)),
            new ValueSpec(() -> defaultValue, validator, context), () -> defaultValue);
    }

    @Override
    public ConfigValue<Boolean> define(String path, boolean defaultValue) {
        return define(path, defaultValue,
            v -> v instanceof Boolean || (v instanceof String s && (s.equals("true") || s.equals("false"))));
    }

    @Override
    public <T> ConfigValue<T> define(String path, T defaultValue) {
        Objects.requireNonNull(defaultValue, "Default value can not be null");
        return define(path, defaultValue, o -> o != null && defaultValue.getClass().isAssignableFrom(o.getClass()));
    }

    @Override
    public <T extends Enum<T>> ConfigValue<T> defineEnum(String path, T defaultValue) {
        Objects.requireNonNull(defaultValue, "Default value can not be null");
        return define(path, defaultValue, o -> o != null && defaultValue.getClass().isAssignableFrom(o.getClass()));
    }

    @Override
    public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<T> defaultValue,
                                                                   Supplier<T> createElement,
                                                                   Predicate<Object> validator) {
        Objects.requireNonNull(defaultValue, "Default value can not be null");
        return define(Lists.newArrayList(pathSplitter.split(path)), new ListValueSpec(() -> defaultValue, createElement,
            x -> x instanceof List<?> l && l.stream().allMatch(validator), validator, context), () -> defaultValue);
    }

    public <T> ConfigValue<T> define(List<String> path, ValueSpec valueSpec, Supplier<T> defaultValue) {
        if (!currentPath.isEmpty()) {
            List<String> tmp = new ArrayList<>(currentPath.size() + path.size());
            tmp.addAll(currentPath);
            tmp.addAll(path);
            path = tmp;
        }
        spec.set(path, valueSpec);
        context = new BuilderContext();
        return new ConfigValueImpl<>(this, defaultValue, path, valueSpec);
    }

    @Override
    public IConfigBuilder worldRestart() {
        context.restartType = RestartType.World;
        return this;
    }

    @Override
    public IConfigBuilder gameRestart() {
        context.restartType = RestartType.Game;
        return this;
    }

    @Override
    public IConfigBuilder enterSection(String name) {
        currentPath.add(name);
        return this;
    }

    @Override
    public IConfigBuilder exitSection() {
        currentPath.removeLast();
        return this;
    }

    @Override
    public IConfig build() {
        context.ensureEmpty();
        @SuppressWarnings("deprecation") Config valueCfg = Config.of(Config.getDefaultMapCreator(true, true),
            InMemoryFormat.withSupport(ConfigValueImpl.class::isAssignableFrom));
        values.forEach(v -> valueCfg.set(v.getPath(), v));
        var cfg = new ConfigImpl(this.spec, valueCfg);
        values.forEach(v -> v.parent = cfg);
        return cfg;
    }

    static class BuilderContext {
        @Nullable String currentTranslation = null;
        RestartType restartType = RestartType.None;
        @Nullable Class<?> clazz;

        public void ensureEmpty() {
            if (currentTranslation != null)
                throw new IllegalStateException("Non-null translation key when building the config");
            if (restartType != RestartType.None)
                throw new IllegalStateException("Dangling restart value set to " + restartType);
        }
    }
}
