package net.fishinghacks.utils.modules;

import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.values.ModuleToggle;

public abstract class IModule {
    public final String name() {
        return this.getClass().getAnnotation(Module.class).name();
    }

    public final ModuleCategory category() {
        return this.getClass().getAnnotation(Module.class).category();
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onToggle() {
    }

    public void buildConfig(AbstractConfig cfg, ConfigBuilder builder) {
        new ModuleToggle(cfg, builder, name());
    }

    public boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) this.onEnable();
        else this.onDisable();
        this.onToggle();
    }

    public final void toggle() {
        setEnabled(!enabled);
    }
}
