package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;

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
