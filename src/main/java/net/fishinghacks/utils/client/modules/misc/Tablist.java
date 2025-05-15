package net.fishinghacks.utils.client.modules.misc;

import net.fishinghacks.utils.client.modules.Module;
import net.fishinghacks.utils.client.modules.ModuleCategory;
import net.fishinghacks.utils.common.config.CachedValue;
import net.fishinghacks.utils.common.config.Config;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Tablist extends Module {
    public static boolean isEnabled = false;
    public static boolean showSuffix = false;
    public static boolean showHeader = true;
    public static boolean showFooter = true;

    private CachedValue<Boolean> showSuffixVal;
    private CachedValue<Boolean> showHeaderVal;
    private CachedValue<Boolean> showFooterVal;

    @Override
    public void buildConfig(Config cfg, ModConfigSpec.Builder builder) {
        super.buildConfig(cfg, builder);

        showSuffixVal = CachedValue.wrap(cfg, builder.define("nametag_suffixes", false));
        showSuffixVal.onInvalidate(() -> showSuffix = showSuffixVal.get());
        showHeaderVal = CachedValue.wrap(cfg, builder.define("show_header", true));
        showHeaderVal.onInvalidate(() -> showHeader = showHeaderVal.get());
        showFooterVal = CachedValue.wrap(cfg, builder.define("show_footer", true));
        showFooterVal.onInvalidate(() -> showFooter = showFooterVal.get());
    }

    @Override
    public String name() {
        return "tablist";
    }

    @Override
    public void onToggle() {
        super.onToggle();
        isEnabled = enabled;
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.MISC;
    }
}
