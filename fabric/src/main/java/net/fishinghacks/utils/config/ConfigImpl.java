package net.fishinghacks.utils.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import net.fishinghacks.utils.platform.services.IConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ConfigImpl implements IConfig {
    private final UnmodifiableConfig spec;
    private final UnmodifiableConfig values;
    @Nullable LoadedConfig loadedConfig;

    public ConfigImpl(UnmodifiableConfig spec, UnmodifiableConfig values) {
        this.spec = spec;
        this.values = values;
    }

    @Override
    public boolean isLoaded() {
        return loadedConfig != null;
    }

    public void load(LoadedConfig cfg) {
        this.loadedConfig = cfg;
        if (cfg != null) {
            correct(spec, cfg.config());
            cfg.save();
        }
        clearCache(values);
    }

    private void clearCache(UnmodifiableConfig values) {
        for (var entry : values.entrySet()) {
            if (entry instanceof ConfigValueImpl<?> value && value.spec.restartType == RestartType.None)
                value.clearCache();
            else if (entry instanceof UnmodifiableConfig inner) clearCache(inner);
        }
    }

    private void correct(UnmodifiableConfig spec, UnmodifiableConfig config) {
        Map<String, Object> specMap = spec.valueMap();
        Map<String, Object> configMap = config.valueMap();

        for (var entry : specMap.entrySet()) {
            String key = entry.getKey();
            Object specValue = entry.getValue();
            Object configValue = configMap.get(key);
            if (specValue instanceof Config specCfg) {
                if (configValue instanceof Config valueCfg) correct(specCfg, valueCfg);
                else {
                    Config newValue = ((Config) config).createSubConfig();
                    configMap.put(key, newValue);
                    correct(specCfg, newValue);
                }
            } else {
                ValueSpec valueSpec = (ValueSpec) specValue;
                if (!valueSpec.validator.test(configValue)) {
                    configMap.put(key, valueSpec.defaultValue.get());
                }
            }
        }

        configMap.entrySet().removeIf(entry -> !specMap.containsKey(entry.getKey()));
    }

    public Config createDefaultConfig() {
        Config config = new SynchronizedConfig();
        correct(spec, config);
        return config;
    }

    @Override
    public void save() {
        if (loadedConfig != null) loadedConfig.save();
    }
}