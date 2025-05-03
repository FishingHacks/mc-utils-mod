package net.fishinghacks.utils.client.modules;

import net.fishinghacks.utils.common.config.Config;
import net.neoforged.neoforge.common.ModConfigSpec;

public abstract class Module {
    public abstract String name();
    public abstract ModuleCategory category();
    public void onEnable() {}
    public void onDisable() {}
    public void onToggle() {}
    public void buildConfig(Config cfg, ModConfigSpec.Builder builder) {}

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
