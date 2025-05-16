package net.fishinghacks.utils.modules;

import net.fishinghacks.utils.config.Config;
import net.fishinghacks.utils.platform.services.IConfigBuilder;

public abstract class Module {
    public abstract String name();
    public abstract ModuleCategory category();
    public void onEnable() {}
    public void onDisable() {}
    public void onToggle() {}
    public void buildConfig(Config cfg, IConfigBuilder builder) {}

    public boolean enabled = false;

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if(enabled) this.onEnable();
        else this.onDisable();
        this.onToggle();
    }
    public final void toggle() {
        setEnabled(!enabled);
    }
}
