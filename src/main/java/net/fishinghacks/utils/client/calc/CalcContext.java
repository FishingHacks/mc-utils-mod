package net.fishinghacks.utils.client.calc;

import net.fishinghacks.utils.client.calc.exprs.LiteralValue;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public record CalcContext(Vec3 playerPos, long time, int playerHealth, HashMap<String, LiteralValue> localVariables) {
    public static final HashMap<String, MathFunction> builtinFunctions = new HashMap<>();
    public static final HashMap<String, LiteralValue> builtinVariables = new HashMap<>();
    public static final HashMap<String, CustomFunction> customFunctions = new HashMap<>();
    public static final int ITEMS_PER_STACK = 64;
    // 3*9*64
    public static final int ITEMS_PER_SHULKERBOX = 1728;

    static {
        builtinVariables.put("pi", new LiteralValue(Math.PI));
        builtinVariables.put("e", new LiteralValue(Math.E));
        builtinVariables.put("stack", new LiteralValue(64.0));
        // 3 * 9 * 64
        builtinVariables.put("sb", new LiteralValue(1728.0));
        builtinVariables.put("shulkerbox", new LiteralValue(1728.0));
        builtinVariables.put("shulkerBox", new LiteralValue(1728.0));
        builtinVariables.put("shulker_box", new LiteralValue(1728.0));

        builtinFunctions.put("sqrt", MathFunctions::root);
        builtinFunctions.put("rt", MathFunctions::root);
        builtinFunctions.put("root", MathFunctions::root);
        builtinFunctions.put("sin", singleDouble(Math::sin));
        builtinFunctions.put("cos", singleDouble(Math::cos));
        builtinFunctions.put("tan", singleDouble(Math::tan));
        builtinFunctions.put("arcsin", singleDouble(Math::asin));
        builtinFunctions.put("arccos", singleDouble(Math::acos));
        builtinFunctions.put("arctan", singleDouble(Math::asin));
        builtinFunctions.put("asin", singleDouble(Math::asin));
        builtinFunctions.put("acos", singleDouble(Math::acos));
        builtinFunctions.put("atan", singleDouble(Math::asin));
        builtinFunctions.put("sec", singleDouble(v -> 1.0 / Math.cos(v)));
        builtinFunctions.put("csc", singleDouble(v -> 1.0 / Math.sin(v)));
        builtinFunctions.put("cot", singleDouble(v -> 1.0 / Math.tan(v)));
        builtinFunctions.put("ln", singleDouble(Math::log));
        builtinFunctions.put("log2", singleDouble(v -> Math.log(v) / Math.log(2.0)));
        builtinFunctions.put("log10", singleDouble(Math::log10));
        builtinFunctions.put("log", MathFunctions::log);
        builtinFunctions.put("rad", singleDouble(v -> v / 180 * Math.PI));
        builtinFunctions.put("deg2rad", singleDouble(v -> v / 180 * Math.PI));
        builtinFunctions.put("abs", singleDouble(Math::abs));
        builtinFunctions.put("floor", singleDouble(Math::floor));
        builtinFunctions.put("ceil", singleDouble(Math::ceil));
        builtinFunctions.put("round", singleDouble(v -> (double) Math.round(v)));
        builtinFunctions.put("sign", singleDouble(v -> v < 0 ? -1.0 : 1.0));
        builtinFunctions.put("not", singleDouble(v -> v == 0.0 ? 1.0 : 0.0));
        builtinFunctions.put("fib", MathFunctions::fib);
        builtinFunctions.put("mod", doubleDouble((value, base) -> ((value % base) + base) % base));
        builtinFunctions.put("random", MathFunctions::random);
        builtinFunctions.put("randint", MathFunctions::randint);
        builtinFunctions.put("min", MathFunctions::min);
        builtinFunctions.put("max", MathFunctions::max);
        builtinFunctions.put("toStack", MathFunctions::toStack);
        builtinFunctions.put("to_stack", MathFunctions::toStack);
        builtinFunctions.put("tostack", MathFunctions::toStack);
        builtinFunctions.put("tosb", MathFunctions::toShulkerBox);
        builtinFunctions.put("toshulkerbox", MathFunctions::toShulkerBox);
        builtinFunctions.put("to_shulker_box", MathFunctions::toShulkerBox);
        builtinFunctions.put("to_shulkerbox", MathFunctions::toShulkerBox);
        builtinFunctions.put("toShulkerBox", MathFunctions::toShulkerBox);
        builtinFunctions.put("fromStack", MathFunctions::fromStack);
        builtinFunctions.put("fromstack", MathFunctions::fromStack);
        builtinFunctions.put("from_stack", MathFunctions::fromStack);
        builtinFunctions.put("fromsb", MathFunctions::fromShulkerBox);
        builtinFunctions.put("fromshulkerbox", MathFunctions::fromShulkerBox);
        builtinFunctions.put("from_shulker_box", MathFunctions::fromShulkerBox);
        builtinFunctions.put("from_shulkerbox", MathFunctions::fromShulkerBox);
        builtinFunctions.put("fromShulkerBox", MathFunctions::fromShulkerBox);
        builtinFunctions.put("x", MathFunctions::x);
        builtinFunctions.put("y", MathFunctions::y);
        builtinFunctions.put("z", MathFunctions::z);
        builtinFunctions.put("time", MathFunctions::time);
        builtinFunctions.put("health", MathFunctions::health);
    }

    public static MathFunction singleDouble(Function<Double, Double> fn) {
        return (ignored, args) -> {
            if (args.size() != 1) throw new MathException(Translation.MismatchingArguments.with(1, args.size()));
            else return new LiteralValue(fn.apply(args.getFirst().value()));
        };
    }

    public static MathFunction doubleDouble(BiFunction<Double, Double, Double> fn) {
        return (ignored, args) -> {
            if (args.size() != 2) throw new MathException(Translation.MismatchingArguments.with(2, args.size()));
            else return new LiteralValue(fn.apply(args.getFirst().value(), args.get(1).value()));
        };
    }

    public static CalcContext getDefault() {
        var player = Minecraft.getInstance().player;
        var level = Minecraft.getInstance().level;
        return new CalcContext(player == null ? Vec3.ZERO : player.getPosition(0f),
            level == null ? 0 : level.getDayTime(), player == null ? 20 : (int) player.getHealth(), new HashMap<>());
    }
}
