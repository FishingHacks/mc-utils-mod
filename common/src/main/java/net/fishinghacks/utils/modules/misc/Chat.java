package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.TranslatableEnum;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.values.CachedColorValue;
import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

@Module(name = "chat", category = ModuleCategory.MISC)
public class Chat extends IModule {
    public static Chat instance;

    public CachedValue<TimeFormat> TIME_FORMAT;
    public CachedValue<Boolean> CUSTOM_PLAYER_FORMAT;
    public CachedValue<String> PREFIX_FORMAT;
    public CachedValue<Boolean> INFINITE_HISTORY;
    public CachedValue<Integer> HISTORY_LENGTH;
    public CachedColorValue BACKGROUND;
    public CachedColorValue FOREGROUND;

    public Chat() {
        instance = this;
    }

    @Override
    public void buildConfig(Config cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);

        TIME_FORMAT = CachedValue.wrapEnum(cfg, builder, "time_format", TimeFormat.Disabled);
        CUSTOM_PLAYER_FORMAT = CachedValue.wrap(cfg, builder, "custom_player_format", false);
        PREFIX_FORMAT = CachedValue.wrap(cfg, builder, "prefix_format", "<%p>");
        INFINITE_HISTORY = CachedValue.wrap(cfg, builder, "infinite_history", false);
        HISTORY_LENGTH = CachedValue.wrap(cfg, builder, "history_length", 100,
            v -> v instanceof Integer && (int) v > 0);
        FOREGROUND = CachedColorValue.wrap(cfg, builder, "fg_color", Colors.WHITE.toCol());
        BACKGROUND = CachedColorValue.wrap(cfg, builder, "background", Colors.BLACK.toCol());
    }

    public enum TimeFormat implements TranslatableEnum {
        Disabled, // nothing
        MinuteSecond, // mm:ss
        TwentyFourHours, // hh:mm:ss
        TwelveHours; // hh:mm:ss <am/pm>

        private static final DateTimeFormatter MIN_SEC = new DateTimeFormatterBuilder().appendValue(
            ChronoField.MINUTE_OF_HOUR).appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE).toFormatter();
        private static final DateTimeFormatter TWENTY_FOUR_HOUR = new DateTimeFormatterBuilder().appendValue(
                ChronoField.HOUR_OF_DAY).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR).appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE).toFormatter();
        private static final DateTimeFormatter TWELVE_HOUR = new DateTimeFormatterBuilder().appendValue(
                ChronoField.HOUR_OF_AMPM).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR).appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE).appendLiteral(' ')
            .appendText(ChronoField.AMPM_OF_DAY, TextStyle.SHORT).toFormatter();

        @Override
        public @NotNull Component getTranslatedName() {
            return switch (this) {
                case Disabled -> Translation.ConfigChatTimeformatDisabled.get();
                case MinuteSecond -> Translation.ConfigChatTimeformatMinuteSecond.get();
                case TwentyFourHours -> Translation.ConfigChatTimeformatTwentyFourHours.get();
                case TwelveHours -> Translation.ConfigChatTimeformatTwelveHours.get();
            };
        }

        public String format(TemporalAccessor value) {
            return switch (this) {
                case Disabled -> "";
                case MinuteSecond -> MIN_SEC.format(value);
                case TwentyFourHours -> TWENTY_FOUR_HOUR.format(value);
                case TwelveHours -> TWELVE_HOUR.format(value);
            };
        }
    }
}
