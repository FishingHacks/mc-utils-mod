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

    @SuppressWarnings("EmptyMethod")
    public void onEnable() {
    }

    @SuppressWarnings("EmptyMethod")
    public void onDisable() {
    }

    @SuppressWarnings("EmptyMethod")
    public void onToggle() {
    }

    public void buildConfig(AbstractConfig cfg, ConfigBuilder builder) {
        new ModuleToggle(cfg, builder, name());
    }

    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled && ModuleManager.isEnabled();
    }

    public final void setEnabled(boolean enabled) {
        boolean enabledPreviously = isEnabled();
        this.enabled = enabled;
        if(isEnabled() == enabledPreviously) return;
        if (isEnabled()) this.onEnable();
        else this.onDisable();
        this.onToggle();
    }

    public final void updateEnabled() {
        setEnabled(enabled);
    }

    public final void toggle() {
        setEnabled(!enabled);
    }
}
