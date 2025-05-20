package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;

@Module(name = "tablist", category = ModuleCategory.MISC)
public class Tablist extends IModule {
    public static boolean isEnabled = false;
    public static boolean showSuffix = false;
    public static boolean showHeader = true;
    public static boolean showFooter = true;

    private CachedValue<Boolean> showSuffixVal;
    private CachedValue<Boolean> showHeaderVal;
    private CachedValue<Boolean> showFooterVal;

    @Override
    public void buildConfig(Config cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);

        showSuffixVal = CachedValue.wrap(cfg, builder, "nametag_suffixes", false);
        showSuffixVal.onInvalidate(() -> showSuffix = showSuffixVal.get());
        showHeaderVal = CachedValue.wrap(cfg, builder, "show_header", true);
        showHeaderVal.onInvalidate(() -> showHeader = showHeaderVal.get());
        showFooterVal = CachedValue.wrap(cfg, builder, "show_footer", true);
        showFooterVal.onInvalidate(() -> showFooter = showFooterVal.get());
    }
    @Override
    public void onToggle() {
        super.onToggle();
        isEnabled = enabled;
    }
}
