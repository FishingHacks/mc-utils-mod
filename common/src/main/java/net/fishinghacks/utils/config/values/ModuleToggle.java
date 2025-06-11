package net.fishinghacks.utils.config.values;

import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.modules.ModuleManagerLike;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleToggle extends AbstractCachedValue<Boolean> {
    public static final Map<String, ModuleToggle> toggles = new HashMap<>();
    public static ModuleManagerLike manager = new ModuleManagerLike() {
        @Override
        public boolean isEnabled(String module) {
            return false;
        }

        @Override
        public void setEnabled(String module, boolean value) {

        }
    };
    private final String key;

    public ModuleToggle(AbstractConfig config, ConfigBuilder builder, String key) {
        super(config, builder);
        toggles.put(key, this);
        this.key = key;
        finish(builder);
    }

    @Override
    protected Boolean doGet() {
        return manager.isEnabled(key);
    }

    @Override
    public Boolean getRaw() {
        return manager.isEnabled(key);
    }

    @Override
    public Boolean getDefault() {
        return false;
    }

    @Override
    public boolean isValid(Object value) {
        return value instanceof Boolean;
    }

    @Override
    protected void doSet(Boolean value, boolean save) {
        manager.setEnabled(key, value);
    }

    @Override
    protected void doClearCache() {
    }

    @Override
    protected List<String> getPath() {
        return List.of(key, "enabled");
    }

    @Override
    protected @Nullable String getTranslationKey() {
        return null;
    }
}
