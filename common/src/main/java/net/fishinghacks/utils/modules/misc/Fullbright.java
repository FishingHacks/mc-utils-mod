package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;

@Module(name = "fullbright", category = ModuleCategory.MISC)
public class Fullbright extends IModule {
    public static boolean isEnabled;

    @Override
    public void onToggle() {
        Fullbright.isEnabled = super.enabled;
    }
}
