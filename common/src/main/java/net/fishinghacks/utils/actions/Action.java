package net.fishinghacks.utils.actions;

import com.mojang.blaze3d.platform.InputConstants;
import net.fishinghacks.utils.ModDisabler;
import net.fishinghacks.utils.config.Configs;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Action {
    protected int id;
    protected InputConstants.Key key = InputConstants.UNKNOWN;

    private static final Set<Integer> enabledModules = new HashSet<>();

    public abstract ActionType type();

    public final InputConstants.Key key() {
        return key;
    }

    public void setKey(InputConstants.Key key) {
        this.key = key;
    }

    protected abstract void enable();

    public abstract String getValue();

    public abstract void setValue(String value);

    public abstract @Nullable List<String> validValues();

    public Component formatValue(String value) {
        return Component.literal(value);
    }

    protected abstract void disable();

    public final void setId(int id) {
        this.id = id;
    }

    public final void setEnabled(boolean enabled) {
        if(ModDisabler.isModDisabled()) return;
        if (isEnabled() == enabled) return;
        if (enabled) enabledModules.add(id);
        else enabledModules.remove(id);
        if (enabled) enable();
        else disable();
    }

    public final boolean isEnabled() {
        return enabledModules.contains(id);
    }

    public static boolean onPress(InputConstants.Key key, boolean pressed) {
        if(ModDisabler.isModDisabled()) return false;
        var list = Configs.clientConfig.ACTIONS.get();
        for (int i = 0; i < list.size(); ++i) {
            var action = list.get(i);
            if (action.key().equals(key)) {
                action.setEnabled(pressed);
                list.set(i, action);
                return true;
            }
        }
        return false;
    }
}
