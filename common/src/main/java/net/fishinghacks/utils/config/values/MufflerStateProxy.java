package net.fishinghacks.utils.config.values;

import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MufflerStateProxy extends AbstractCachedValue<Void> {
    private final String key;

    public MufflerStateProxy(Config config, ConfigBuilder builder, String key) {
        super(config, builder);
        this.key = key;
        finish(builder);
    }

    @Override
    protected Void doGet() {
        return null;
    }

    @Override
    public Void getRaw() {
        return null;
    }

    @Override
    public Void getDefault() {
        return null;
    }

    @Override
    public boolean isValid(Object value) {
        return value == null;
    }

    @Override
    protected void doSet(Void ignored0, boolean ignored1) {
    }

    @Override
    protected void doClearCache() {
    }

    @Override
    protected List<String> getPath() {
        return List.of(key);
    }

    @Override
    protected @Nullable String getTranslationKey() {
        return null;
    }
}
