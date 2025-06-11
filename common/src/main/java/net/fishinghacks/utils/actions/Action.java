package net.fishinghacks.utils.actions;

import com.mojang.blaze3d.platform.InputConstants;
import net.fishinghacks.utils.ModDisabler;
import net.fishinghacks.utils.config.Configs;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an action that can be triggered by a key.
 */
public abstract class Action {
    /**
     * The id of the action
     */
    protected int id;
    /**
     * The key that triggers the action
     */
    protected InputConstants.Key key = InputConstants.UNKNOWN;

    /**
     * Creates a new action
     */
    public Action() {
    }

    private static final Set<Integer> enabledModules = new HashSet<>();

    /**
     * Gets the type of action
     *
     * @return Returns the type of the action
     */
    public abstract ActionType type();

    /**
     * Returns the key that triggers the action
     *
     * @return the key
     */
    public final InputConstants.Key key() {
        return key;
    }

    /**
     * Sets the key that is supposed to trigger this action
     *
     * @param key the key
     */
    public void setKey(InputConstants.Key key) {
        this.key = key;
    }

    /**
     * Enables/starts this action, usually when the key is pressed down
     */
    protected abstract void enable();

    /**
     * Gets the String value associated with this action. What this means depends on the action type.
     *
     * @return the value
     * @see #type()
     */
    public abstract String getValue();

    /**
     * Sets the String value associated with this action. What this means depends on the action type.
     *
     * @param value the new value
     * @see #type()
     */
    public abstract void setValue(String value);

    /**
     * Gets a list of values that are valid for the string value. What each of these mean depends on the action type.
     * If any string is allowed, this will return {@code null}
     *
     * @return the list of actions
     */
    public abstract @Nullable List<String> validValues();

    /**
     * Translates a String value into something that can be displayed.
     *
     * @param value the value
     * @return the translated value
     * @see #getValue()
     * @see #setValue(String)
     * @see #type()
     */
    public Component formatValue(String value) {
        return Component.literal(value);
    }

    /**
     * Disables/stops this action, usually when the associated key is released.
     */
    protected abstract void disable();

    /**
     * Sets the id of this action
     *
     * @param id the new id
     */
    public final void setId(int id) {
        this.id = id;
    }

    /**
     * Enables/Disables this action, unless the mod is disabled.
     *
     * @param enabled whether to enable the action or not
     * @see #enable()
     * @see #disable()
     * @see ModDisabler
     */
    public final void setEnabled(boolean enabled) {
        if (ModDisabler.isModDisabled()) enabled = false;
        if (enabledModules.contains(id) == enabled) return;
        if (enabled) enabledModules.add(id);
        else enabledModules.remove(id);
        if (enabled) enable();
        else disable();
    }

    /**
     * Checks if this action is enabled
     *
     * @return the enabled status
     */
    public final boolean isEnabled() {
        return enabledModules.contains(id) && !ModDisabler.isModDisabled();
    }

    /**
     * Handles a key press and returns whether to stop propagating it.
     *
     * @param key The key
     * @param pressed if it was pressed or released
     * @return whether to stop propagating it
     */
    public static boolean onPress(InputConstants.Key key, boolean pressed) {
        if (ModDisabler.isModDisabled()) return false;
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
