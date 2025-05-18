package net.fishinghacks.utils.config.spec;

import net.fishinghacks.utils.config.values.AbstractCachedValue;
import net.fishinghacks.utils.platform.Services;
import net.fishinghacks.utils.platform.services.IConfig;
import net.fishinghacks.utils.platform.services.IConfigBuilder;

import java.util.ArrayList;
import java.util.List;

public final class ConfigBuilder {
    private final IConfigBuilder inner;
    private final List<ConfigSpec> specs;
    public RestartType type = RestartType.None;

    public ConfigBuilder() {
        this.inner = Services.PLATFORM.createConfigBuilder();
        this.specs = new ArrayList<>();
        specs.add(new ConfigSpec(List.of()));
    }

    public void enterSection(String key) {
        inner.enterSection(key);
        specs.add(specs.getLast().createSubconfig(key));
    }

    public void exitSection() {
        if (specs.size() > 1) {
            specs.removeLast();
            inner.exitSection();
        } else throw new IllegalStateException("tried exiting a section, even tho no section was previously entered");
    }

    public void worldRestart() {
        type = RestartType.World;
        inner.worldRestart();
    }
    public void gameRestart() {
        type = RestartType.Game;
        inner.gameRestart();
    }

    public void register(AbstractCachedValue<?> value) {
        specs.getLast().addValue(value);
        type = RestartType.None;
    }

    public ConfigSpec getSpec() {
        return specs.getFirst();
    }

    public IConfig build() {
        return inner.build();
    }

    public IConfigBuilder inner() {
        return inner;
    }
}
