package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;

@Module(name = "no_key_block", category = ModuleCategory.MISC)
public class AllowBlockedKeys extends IModule {
    public static AllowBlockedKeys instance;

    public AllowBlockedKeys() {
        instance = this;
    }
}
