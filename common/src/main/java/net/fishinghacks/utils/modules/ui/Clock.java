package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.RenderableTextModule;
import net.minecraft.network.chat.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.List;

@Module(name = "clock", category = ModuleCategory.UI)
public class Clock extends RenderableTextModule {
    private CachedValue<Boolean> twentyFourHours;
    private CachedValue<Boolean> showSeconds;
    private CachedValue<Boolean> showDate;

    private DateTimeFormatter formatter;

    private DateTimeFormatter buildFormatter() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        if (showDate.get()) builder.appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.YEAR, 4)
            .appendLiteral(" ");
        if (twentyFourHours.get()) builder.appendValue(ChronoField.HOUR_OF_DAY, 2);
        else builder.appendValue(ChronoField.HOUR_OF_AMPM, 2);
        builder.appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2);
        if (showSeconds.get()) builder.appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2);
        if (!twentyFourHours.get()) builder.appendLiteral(' ').appendText(ChronoField.AMPM_OF_DAY, TextStyle.SHORT);
        return builder.toFormatter();
    }

    @Override
    public void buildConfig(Config cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);
        twentyFourHours = CachedValue.wrap(cfg, builder, "24_hour_format", true);
        showSeconds = CachedValue.wrap(cfg, builder, "show_seconds", false);
        showDate = CachedValue.wrap(cfg, builder, "show_date", false);
        cfg.addCachedValue(() -> formatter = buildFormatter());
    }

    @Override
    public List<Component> getText() {
        return List.of(Component.literal(LocalDateTime.now().format(formatter)));
    }

    @Override
    public List<Component> getPreviewText() {
        return List.of(Component.literal(LocalDateTime.of(0, 1, 1, 0, 0, 0).format(formatter)));
    }
}
