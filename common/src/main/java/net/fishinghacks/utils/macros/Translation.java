package net.fishinghacks.utils.macros;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

public enum Translation implements Supplier<Component> {
    MismatchingArguments("mismatching_arguments"), CalledFromNote("called_from_note"), MismatchingArgumentsRange(
        "mismatching_arguments.range"), UnboundVariable("unbound_variable"), UnboundFunction(
        "unbound_function"), UnexpectedChar("unexpected_char"), ExpectedTokenType(
        "expected_tokentype"), ExpectedTokenTypeButNone("expected_tokentype.none"), ExpectedTokenTypes(
        "expected_tokentype_many"), ExpectedTokenTypesButNone("expected_tokentype_many.none"), ExpectedToken(
        "expected_tokentype.some"), InvalidNumber("invalid_number"), ExpectedEnd("expected_end"), RandomMinGreaterEqMax(
        "random_min_greater_eq_max"), TypeNumber("type.number"), TypeString("type.string"), TypeBoolean(
        "type.boolean"), TypeList("type.list"), TypeMap("type.map"), TypeFunction("type.function"), TypeNull(
        "type.null"), TypeUserData("type.userdata"), CannotIndexType("cannot_index_type"), CannotIterate("cannot_iterate"), LoopKwInvalid(
        "loop_kw_outside_loop"), ReturnKwInvalid("return_kw_outside_function"), WasInterrupted("was_interrupted"), IsNotCallable("is_not_callable");

    private final String key;
    private final Component component;

    Translation(String key) {
        this.key = "utils.macro." + key;
        component = Component.translatable(this.key);
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
}