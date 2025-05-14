package net.fishinghacks.utils.client.calc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Supplier;

public enum Translation implements Supplier<Component> {
    MismatchingArguments("mismatching_arguments"), MismatchingArgumentsRange(
        "mismatching_arguments.range"), UnboundVariable("unbound_variable"), UnboundFunction(
        "unbound_function"), UnexpectedChar("unexpected_char"), ExpectedTokenType(
        "expected_tokentype"), ExpectedTokenTypeButNone("expected_tokentype.none"), ExpectedTokenTypes(
        "expected_tokentype_many"), ExpectedTokenTypesButNone("expected_tokentype_many.none"), ExpectedToken(
        "expected_tokentype.some"), InvalidNumber("invalid_number"), ExpectedEnd("expected_end"), RandomMinGreaterEqMax("random_min_greater_eq_max");

    private final String key;
    private final Component component;

    Translation(String key) {
        this.key = "utils.calc." + key;
        component = Component.translatable(key);
    }

    public String key() {
        return key;
    }

    public MutableComponent with(Object... args) {
        return Component.translatable(this.key, args);
    }

    public Component get() {
        return component;
    }

    public ModConfigSpec.Builder config(ModConfigSpec.Builder builder) {
        return builder.translation(this.key);
    }
}