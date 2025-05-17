package net.fishinghacks.utils.config;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ValueSpec {
    public final @Nullable String langKey;
    public final @Nullable Class<?> clazz;
    public final Supplier<?> defaultValue;
    public final Predicate<Object> validator;
    public final RestartType restartType;

    ValueSpec(Supplier<?> defaultValue, Predicate<Object> validator, ConfigBuilderImpl.BuilderContext context) {
        this.langKey = context.currentTranslation;
        this.clazz = context.clazz;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.restartType = context.restartType;
    }
}
