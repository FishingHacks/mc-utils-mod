package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.TranslatableEnum;
import net.fishinghacks.utils.macros.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LiteralValue implements Expression {
    private final @NotNull Object value;
    private final ValueType type;
    public static final LiteralValue NULL = new LiteralValue(new NullValue(), ValueType.Null);

    private LiteralValue(@NotNull Object value, ValueType type) {
        this.value = value;
        this.type = type;
    }

    public LiteralValue(double value) {
        this(value, ValueType.Number);
    }

    public LiteralValue(boolean value) {
        this(value, ValueType.Boolean);
    }

    public LiteralValue(@NotNull String value) {
        this(value, ValueType.String);
    }

    public LiteralValue(@NotNull List<LiteralValue> value) {
        this(value, ValueType.List);
    }

    public LiteralValue(@NotNull Map<String, LiteralValue> value) {
        this(value, ValueType.Map);
    }

    public LiteralValue(@NotNull FunctionValue value) {
        this(value, ValueType.Function);
    }

    public LiteralValue(@NotNull BuiltinFunctionValue value) {
        this(value, ValueType.BuiltinFunction);
    }

    public LiteralValue(@NotNull UserData data) {
        this(data, ValueType.UserData);
    }

    @Override
    public LiteralValue eval(EvalContext context) {
        return this;
    }

    public static String doubleToString(double value) {
        return value == Math.floor(value) ? "" + (long) Math.floor(value) : "" + value;
    }

    public boolean equal(LiteralValue other) {
        return type.equals(other.type) && value.equals(other.value);
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, LiteralValue>> asMap() {
        return type == ValueType.Map ? Optional.of((Map<String, LiteralValue>) value) : Optional.empty();
    }

    public Optional<FunctionValue> asFunction() {
        return type == ValueType.Function ? Optional.of((FunctionValue) value) : Optional.empty();
    }

    public Optional<BuiltinFunctionValue> asBuiltinFunction() {
        return type == ValueType.BuiltinFunction ? Optional.of((BuiltinFunctionValue) value) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public Optional<List<LiteralValue>> asList() {
        return type == ValueType.List ? Optional.of((List<LiteralValue>) value) : Optional.empty();
    }

    public Optional<UserData> asUserData() {
        return type == ValueType.UserData ? Optional.of((UserData) value) : Optional.empty();
    }

    @Override
    public String toString() {
        if (type == ValueType.UserData) return value.toString();
        if (type != ValueType.String) return asString();
        return "\"" + ((String) value).replaceAll("\\\\", "\\\\").replaceAll("\"", "\\\\\"").replaceAll("\\n", "\\\\n")
            .replaceAll("\\t", "\\\\t") + "\"";
    }

    public boolean isNull() {
        return type == ValueType.Null;
    }

    public boolean asBoolean() {
        return switch (type) {
            case Number -> ((double) value) != 0.0;
            case String -> !((String) value).isEmpty();
            case Boolean -> (boolean) value;
            case List, Function, BuiltinFunction, Map, UserData -> true;
            case Null -> false;
        };
    }

    @SuppressWarnings("unchecked")
    public String asString() {
        return switch (type) {
            case Number -> doubleToString((double) value);
            case String -> (String) value;
            case Boolean -> String.valueOf((boolean) value);
            case List -> {
                var values = (List<LiteralValue>) value;
                var builder = new StringBuilder();
                values.forEach(val -> {
                    if (!builder.isEmpty()) builder.append(", ");
                    builder.append(val.asString());
                });
                yield "[" + builder + "]";
            }
            case Map -> {
                var values = (Map<String, LiteralValue>) value;
                var builder = new StringBuilder();
                values.forEach((key, value) -> {
                    builder.append("\n    ").append(key).append(": ");
                    var lines = value.asString().lines().iterator();
                    builder.append(lines.next());
                    lines.forEachRemaining(line -> builder.append("\n    ").append(line));
                    builder.append(",");
                });
                yield builder.isEmpty() ? "{}" : "{" + builder + "\n}";
            }
            case Function -> "<function " + ((FunctionValue) value).name() + ">";
            case BuiltinFunction -> "<built-in fuction " + ((BuiltinFunctionValue) value).name() + ">";
            case Null -> "null";
            case UserData -> ((UserData) value).asString();
        };
    }

    public double asDouble() {
        return switch (type) {
            case Number -> (double) value;
            case Boolean -> ((boolean) value) ? 1 : 0;
            default -> 0.0;
        };
    }

    public ValueType type() {
        return type;
    }

    public List<Component> formatted() {
        return formatted(0);
    }

    @SuppressWarnings("unchecked")
    public List<Component> formatted(int indent) {
        return switch (this.type) {
            case Number -> {
                String value = doubleToString((double) this.value);
                yield List.of(copyable(Component.literal(value).withStyle(ChatFormatting.GREEN), value));
            }
            case String ->
                List.of(copyable(Component.literal(toString()).withStyle(ChatFormatting.YELLOW), asString()));
            case List -> {
                var list = new ArrayList<Component>();
                var comp = Component.literal("[");
                for (var entry : (List<LiteralValue>) value) {
                    var components = entry.formatted(indent);
                    if (components.isEmpty()) continue;
                    comp.append(components.removeFirst());
                    list.add(comp);
                    comp = components.removeLast().copy();
                    list.addAll(components);
                }
                list.add(comp.append("]"));
                yield list;
            }
            case Map -> {
                if (((Map<String, LiteralValue>) value).isEmpty()) yield List.of(Component.literal("{}"));
                var list = new ArrayList<Component>();
                var comp = Component.literal("[");
                for (var entry : ((Map<String, LiteralValue>) value).entrySet()) {
                    list.add(comp);
                    comp = Component.literal("    ".repeat(indent + 1)).append(entry.getKey()).append(": ");
                    var components = entry.getValue().formatted(indent);
                    if (!components.isEmpty()) {
                        comp.append(components.removeFirst());
                        list.add(comp);
                        comp = components.removeLast().copy();
                        list.addAll(components);
                    }
                    comp.append(",");
                }
                list.add(comp);
                list.add(Component.literal("    ".repeat(indent)).append("}"));
                yield list;
            }
            case Boolean, Function, Null, BuiltinFunction, UserData ->
                List.of(Component.literal(toString()).withStyle(ChatFormatting.LIGHT_PURPLE));
        };
    }

    private MutableComponent copyable(MutableComponent comp, String value) {
        return comp.withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(value)));
    }


    public enum ValueType implements TranslatableEnum {
        Number, String, Boolean, List, Map, Function, Null, BuiltinFunction, UserData;

        @Override
        public @NotNull Component getTranslatedName() {
            return switch (this) {
                case Number -> Translation.TypeNumber.get();
                case String -> Translation.TypeString.get();
                case Boolean -> Translation.TypeBoolean.get();
                case List -> Translation.TypeList.get();
                case Map -> Translation.TypeMap.get();
                case Function, BuiltinFunction -> Translation.TypeFunction.get();
                case Null -> Translation.TypeNull.get();
                case UserData -> Translation.TypeUserData.get();
            };
        }
    }

    private static class NullValue {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof NullValue;
        }
    }
}