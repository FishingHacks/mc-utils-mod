package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.exprs.Expression;
import net.fishinghacks.utils.macros.exprs.LiteralValue;
import net.fishinghacks.utils.macros.parsing.Location;
import net.fishinghacks.utils.macros.parsing.Parser;
import net.fishinghacks.utils.macros.statements.ExprStatement;
import net.fishinghacks.utils.macros.statements.FunctionStatement;
import net.fishinghacks.utils.macros.statements.Statement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Executor {
    public static final HashMap<String, LiteralValue> defaultGlobalScope = new HashMap<>();

    private static void addFn(String key, MathFunction function) {
        defaultGlobalScope.put(key, new LiteralValue(BuiltinFunctionValue.withShouldExitCheck(key, function)));
    }

    public static RunningMacro runInThread(String contents, String file,
                                           HashMap<String, LiteralValue> customGlobalScope,
                                           Consumer<Component> outputError, Consumer<LiteralValue> output,
                                           Function<String, Optional<String>> getFileContents, Runnable onFinish) {
        var shouldStop = new AtomicBoolean();
        var thread = new Thread(() -> {
            try {
                output.accept(parseAndExecute(contents + ";", file, customGlobalScope, shouldStop));
            } catch (MathException e) {
                e.location.print(getFileContents, file, contents, e.message, outputError);
                Throwable cause = e;
                while ((cause = cause.getCause()) != null) {
                    if (cause instanceof MathException err)
                        err.location.print(getFileContents, file, contents, err.message, outputError);
                    else outputError.accept(
                        Component.literal("Error: " + cause.getMessage()).withStyle(ChatFormatting.RED));
                }
            } finally {
                Minecraft.getInstance().schedule(onFinish);
            }
        });
        thread.start();
        return new RunningMacro(thread, shouldStop);
    }

    public static LiteralValue parseAndExecute(String contents, String file,
                                               HashMap<String, LiteralValue> customGlobalScope,
                                               AtomicBoolean shouldStop) throws MathException {
        var statements = new ArrayList<Statement>();
        var globalScope = new HashMap<String, LiteralValue>();
        globalScope.putAll(defaultGlobalScope);
        globalScope.putAll(customGlobalScope);
        var parser = new Parser(contents, file);
        while (!parser.atEnd()) {
            var statement = parser.parseStatement();
            if (statement instanceof FunctionStatement fn) globalScope.put(fn.name, new LiteralValue(fn.value));
            else statements.add(statement);
        }
        Expression finalExpr = null;
        if (statements.getLast() instanceof ExprStatement e) {
            finalExpr = e.expr;
            statements.removeLast();
        }
        var evalContext = EvalContext.create(globalScope, shouldStop);
        try {
            for (var statement : statements)
                statement.execute(evalContext);
        } catch (BreakoutException e) {
            throw new MathException(Translation.WasInterrupted.get(), Location.ZERO);
        }
        if (finalExpr == null) return LiteralValue.NULL;
        try {
            return finalExpr.eval(evalContext);
        } catch (BreakoutException e) {
            throw new MathException(Translation.WasInterrupted.get(), Location.ZERO);
        }
    }

    public static void checkExit(EvalContext ctx) throws BreakoutException.EvalShouldStop {
        if (ctx.shouldStop().get()) throw new BreakoutException.EvalShouldStop();
    }

    public static void sleep(EvalContext ctx,
                             Duration duration) throws MathException, BreakoutException.EvalShouldStop {
        try {
            if (!duration.isZero() && !duration.isNegative()) Thread.sleep(duration);
        } catch (InterruptedException e) {
            if (ctx.shouldStop().get()) throw new BreakoutException.EvalShouldStop();
            else throw new MathException(
                Component.literal("INTERNAL ERROR : UNEXPECTED INTERRUPT EXCEPTION DURING " + "SLEEP"), Location.ZERO);
        }
        if (ctx.shouldStop().get()) throw new BreakoutException.EvalShouldStop();
    }

    public static void addSingleDouble(String key, Function<Double, Double> fn) {
        var location = Location.builtin("function_" + key);
        addFn(key, (ignored, args) -> {
            if (args.size() != 1)
                throw new MathException(Translation.MismatchingArguments.with(1, args.size()), location);
            else return new LiteralValue(fn.apply(args.getFirst().asDouble()));
        });
    }

    public static void addDoubleDouble(String key, BiFunction<Double, Double, Double> fn) {
        var location = Location.builtin("function_" + key);
        addFn(key, (ignored, args) -> {
            if (args.size() != 2)
                throw new MathException(Translation.MismatchingArguments.with(2, args.size()), location);
            else return new LiteralValue(fn.apply(args.getFirst().asDouble(), args.get(1).asDouble()));
        });
    }

    static {
        defaultGlobalScope.put("pi", new LiteralValue(Math.PI));
        defaultGlobalScope.put("e", new LiteralValue(Math.E));
        defaultGlobalScope.put("stack", new LiteralValue(64.0));
        // 3 * 9 * 64
        defaultGlobalScope.put("sb", new LiteralValue(1728.0));
        defaultGlobalScope.put("shulkerbox", new LiteralValue(1728.0));
        defaultGlobalScope.put("shulkerBox", new LiteralValue(1728.0));
        defaultGlobalScope.put("shulker_box", new LiteralValue(1728.0));

        addSingleDouble("sin", Math::sin);
        addSingleDouble("cos", Math::cos);
        addSingleDouble("tan", Math::tan);
        addSingleDouble("arcsin", Math::asin);
        addSingleDouble("arccos", Math::acos);
        addSingleDouble("arctan", Math::asin);
        addSingleDouble("asin", Math::asin);
        addSingleDouble("acos", Math::acos);
        addSingleDouble("atan", Math::asin);
        addSingleDouble("sec", v -> 1.0 / Math.cos(v));
        addSingleDouble("csc", v -> 1.0 / Math.sin(v));
        addSingleDouble("cot", v -> 1.0 / Math.tan(v));
        addSingleDouble("ln", Math::log);
        addSingleDouble("log2", v -> Math.log(v) / Math.log(2.0));
        addSingleDouble("log10", Math::log10);
        addSingleDouble("rad", v -> v / 180 * Math.PI);
        addSingleDouble("deg2rad", v -> v / 180 * Math.PI);
        addSingleDouble("abs", Math::abs);
        addSingleDouble("floor", Math::floor);
        addSingleDouble("ceil", Math::ceil);
        addSingleDouble("round", v -> (double) Math.round(v));
        addSingleDouble("sign", v -> v < 0 ? -1.0 : 1.0);
        addSingleDouble("not", v -> v == 0.0 ? 1.0 : 0.0);
        addDoubleDouble("mod", (value, base) -> ((value % base) + base) % base);

        addFn("sqrt", BuiltinFunctions::root);
        addFn("rt", BuiltinFunctions::root);
        addFn("root", BuiltinFunctions::root);
        addFn("log", BuiltinFunctions::log);
        addFn("fib", BuiltinFunctions::fib);
        addFn("random", BuiltinFunctions::random);
        addFn("randint", BuiltinFunctions::randint);
        addFn("min", BuiltinFunctions::min);
        addFn("max", BuiltinFunctions::max);
        addFn("sleep", BuiltinFunctions::sleep);

        addSingleDouble("toStack", v -> v / 64.0);
        addSingleDouble("to_stack", v -> v / 64.0);
        addSingleDouble("tostack", v -> v / 64.0);

        // 3 * 9 * 64
        addSingleDouble("tosb", v -> v / 1728.0);
        addSingleDouble("toshulkerbox", v -> v / 1728.0);
        addSingleDouble("to_shulker_box", v -> v / 1728.0);
        addSingleDouble("to_shulkerbox", v -> v / 1728.0);
        addSingleDouble("toShulkerBox", v -> v / 1728.0);

        addSingleDouble("fromStack", v -> v * 64.0);
        addSingleDouble("fromstack", v -> v * 64.0);
        addSingleDouble("from_stack", v -> v * 64.0);

        // 3 * 9 * 64
        addSingleDouble("fromsb", v -> v * 1728.0);
        addSingleDouble("fromshulkerbox", v -> v * 1728.0);
        addSingleDouble("from_shulker_box", v -> v * 1728.0);
        addSingleDouble("from_shulkerbox", v -> v * 1728.0);
        addSingleDouble("fromShulkerBox", v -> v * 1728.0);
    }
}
