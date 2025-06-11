package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.config.values.CachedColorValue;
import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;

@Module(name = "scoreboard", category = ModuleCategory.MISC)
public class Scoreboard extends IModule {
    public static Scoreboard instance;

    public CachedValue<Boolean> NUMBERS;
    public CachedValue<Boolean> NUMBER_WIDTH;
    public CachedValue<Boolean> TEXT_SHADOW;
    public CachedValue<Boolean> TEXT_SHADOW_TITLE;
    public CachedColorValue BACKGROUND;
    public CachedColorValue TITLE_BACKGROUND;
    public CachedColorValue TEXT_COLOR;
    public CachedColorValue TITLE_TEXT_COLOR;
    public CachedValue<Boolean> TITLE;

    public Scoreboard() {
        instance = this;
    }

    @Override
    public void buildConfig(AbstractConfig cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);

        NUMBERS = CachedValue.wrap(cfg, builder, "numbers", true);
        NUMBER_WIDTH = CachedValue.wrap(cfg, builder, "number_width", true);
        TEXT_SHADOW = CachedValue.wrap(cfg, builder, "text_shadow", false);
        BACKGROUND = CachedColorValue.wrap(cfg, builder, "background", Colors.BLACK.toCol().withAlpha(.3f));
        TEXT_COLOR = CachedColorValue.wrap(cfg, builder, "fg_color", Colors.WHITE.toCol());
        TITLE = CachedValue.wrap(cfg, builder, "title", true);
        TEXT_SHADOW_TITLE = CachedValue.wrap(cfg, builder, "title_text_shadow", false);
        TITLE_BACKGROUND = CachedColorValue.wrap(cfg, builder, "title_background", Colors.BLACK.toCol().withAlpha(.4f));
        TITLE_TEXT_COLOR = CachedColorValue.wrap(cfg, builder, "title_text_color", Colors.WHITE.toCol());
    }
}
