package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.BreakoutException.EvalShouldStop;
import net.fishinghacks.utils.macros.exprs.LiteralValue;
import net.fishinghacks.utils.macros.parsing.Location;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BuiltinFunctions {
    private static final Location rootLoc = Location.builtin("root");

    public static LiteralValue root(EvalContext ignored, List<LiteralValue> args) throws MathException {
        if (args.isEmpty() || args.size() > 2)
            throw new MathException(Translation.MismatchingArgumentsRange.with(1, 2, args.size()), rootLoc);
        if (args.size() == 1) return new LiteralValue(Math.sqrt(args.getFirst().asDouble()));
        return new LiteralValue(Math.pow(args.get(1).asDouble(), 1.0 / args.getFirst().asDouble()));
    }

    private static final Location logLoc = Location.builtin("log");

    public static LiteralValue log(EvalContext ignored, List<LiteralValue> args) throws MathException {
        if (args.size() != 2) throw new MathException(Translation.MismatchingArguments.with(2, args.size()), logLoc);
        return new LiteralValue(Math.log(args.get(0).asDouble()) / Math.log(args.get(1).asDouble()));
    }

    private static final Location fibLoc = Location.builtin("fib");

    public static LiteralValue fib(EvalContext ctx, List<LiteralValue> args) throws MathException, EvalShouldStop {
        if (args.size() != 1) throw new MathException(Translation.MismatchingArguments.with(1, args.size()), fibLoc);
        long value = (long) Math.floor(args.getFirst().asDouble());
        if (value < 2) return new LiteralValue((double) value);
        long a = 0;
        long b = 1;
        for (long i = 1; i < value; ++i) {
            Executor.checkExit(ctx);
            long tmp = a + b;
            a = b;
            b = tmp;
        }
        return new LiteralValue((double) b);
    }

    private static final Location randomLoc = Location.builtin("random");

    public static LiteralValue random(EvalContext ignored, List<LiteralValue> args) throws MathException {
        if (args.size() > 2)
            throw new MathException(Translation.MismatchingArgumentsRange.with(0, 2, args.size()), randomLoc);
        double min = args.size() == 2 ? args.getFirst().asDouble() : 0.0;
        double max = args.size() == 1 ? args.getFirst().asDouble() : args.size() == 2 ? args.get(1).asDouble() : 1.0;
        if (min >= max) throw new MathException(Translation.RandomMinGreaterEqMax.with(min, max), randomLoc);
        return new LiteralValue(ThreadLocalRandom.current().nextDouble(min, max));
    }

    private static final Location randIntLoc = Location.builtin("randint");

    public static LiteralValue randint(EvalContext ignored, List<LiteralValue> args) throws MathException {
        if (args.size() != 2)
            throw new MathException(Translation.MismatchingArguments.with(2, args.size()), randIntLoc);
        long min = (long) Math.floor(args.get(0).asDouble());
        long max = (long) Math.floor(args.get(1).asDouble());
        if (min >= max) throw new MathException(Translation.RandomMinGreaterEqMax.with(min, max), randIntLoc);
        return new LiteralValue((double) ThreadLocalRandom.current().nextLong(min, max + 1));
    }

    public static LiteralValue min(EvalContext ignored, List<LiteralValue> args) {
        return new LiteralValue(args.stream().map(LiteralValue::asDouble).reduce(0.0, Math::min));
    }

    public static LiteralValue max(EvalContext ignored, List<LiteralValue> args) {
        return new LiteralValue(args.stream().map(LiteralValue::asDouble).reduce(0.0, Math::min));
    }

    private static final Location sleepLoc = Location.builtin("sleep");

    public static LiteralValue sleep(EvalContext ctx, List<LiteralValue> args) throws MathException, EvalShouldStop {
        if (args.size() != 1) throw new MathException(Translation.MismatchingArguments.with(1, args.size()), sleepLoc);
        Executor.sleep(ctx, Duration.ofMillis((long) Math.floor(args.getFirst().asDouble())));
        return LiteralValue.NULL;
    }
}
