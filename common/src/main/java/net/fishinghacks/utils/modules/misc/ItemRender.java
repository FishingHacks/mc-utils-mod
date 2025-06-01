package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.ui.ArmorStatus;

@Module(name = "item_render", category = ModuleCategory.MISC)
public class ItemRender extends IModule {
    public static ItemRender instance;

    public CachedValue<Boolean> showAmount;
    public CachedValue<Boolean> showCooldown;
    public CachedValue<Boolean> showEnchantmentGlint;
    public CachedValue<ArmorStatus.DurabilityDisplay> durabilityDisplay;

    public ItemRender() {
        instance = this;
    }

    @Override
    public void buildConfig(Config cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);

        showAmount = CachedValue.wrap(cfg, builder, "show_amount", true);
        showCooldown = CachedValue.wrap(cfg, builder, "show_cooldown", true);
        showEnchantmentGlint = CachedValue.wrap(cfg, builder, "show_enchantment_glint", true);
        durabilityDisplay = CachedValue.wrapEnum(cfg, builder, "durability_display", ArmorStatus.DurabilityDisplay.Bar);
    }
}
