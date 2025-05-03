package net.fishinghacks.utils.client.modules.misc;

import net.fishinghacks.utils.client.modules.Module;
import net.fishinghacks.utils.client.modules.ModuleCategory;

public class AllowBlockedKeys extends Module {
    public static boolean allowKeys;

    @Override
    public void onToggle() {
        allowKeys = enabled;
    }

    @Override
    public String name() {
        return "no_key_block";
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.MISC;
    }
}
