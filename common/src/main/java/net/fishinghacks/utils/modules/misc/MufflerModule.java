package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.values.MufflerStateProxy;
import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;

@Module(name = "muffler", category = ModuleCategory.MISC)
public class MufflerModule extends IModule {
    public static boolean isEnabled;

    @Override
    public void onToggle() {
        MufflerModule.isEnabled = enabled;
    }

    @Override
    public void buildConfig(Config cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);
        new MufflerStateProxy(cfg, builder, "muffler_state");
    }
}