package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;

@Module(name = "fullbright", category = ModuleCategory.MISC)
public class Fullbright extends IModule {
    public static Fullbright instance;

    public Fullbright() {
        instance = this;
    }
}
