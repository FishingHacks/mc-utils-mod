package net.fishinghacks.utils.client.modules.misc;

import net.fishinghacks.utils.client.modules.Module;
import net.fishinghacks.utils.client.modules.ModuleCategory;

public class Fullbright extends Module {
    public static boolean isEnabled;

    @Override
    public void onToggle() {
        Fullbright.isEnabled = super.enabled;
    }

    @Override
    public String name() {
        return "fullbright";
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.MISC;
    }
}
