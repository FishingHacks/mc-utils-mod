package net.fishinghacks.utils.config;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ListValueSpec extends ValueSpec {
    private final Predicate<Object> elementValidator;
    private final @Nullable Supplier<?> newElementSupplier;


    ListValueSpec(Supplier<?> defaultValue, @Nullable Supplier<?> newElementSupplier, Predicate<Object> listValidator,
                  Predicate<Object> elementValidator, ConfigBuilderImpl.BuilderContext context) {
        super(defaultValue, listValidator, context);
        this.newElementSupplier = newElementSupplier;
        this.elementValidator = elementValidator;
    }

    public @Nullable Supplier<?> getNewElementSupplier() {
        return this.newElementSupplier;
    }
    public boolean testElement(Object value) {
        return this.elementValidator.test(value);
    }
}
