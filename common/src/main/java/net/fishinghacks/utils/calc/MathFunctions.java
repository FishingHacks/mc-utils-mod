package net.fishinghacks.utils.calc;

import net.fishinghacks.utils.calc.exprs.LiteralValue;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MathFunctions {
    public static LiteralValue root(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (args.isEmpty() || args.size() > 2)
            throw new MathException(Translation.MismatchingArgumentsRange.with(1, 2, args.size()));
        if (args.size() == 1) return new LiteralValue(Math.sqrt(args.getFirst().value()));
        return new LiteralValue(Math.pow(args.get(1).value(), 1.0 / args.getFirst().value()));
    }

    public static LiteralValue log(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (args.size() != 2) throw new MathException(Translation.MismatchingArguments.with(2, args.size()));
        return new LiteralValue(Math.log(args.get(0).value()) / Math.log(args.get(1).value()));
    }

    public static LiteralValue fib(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (args.size() != 1) throw new MathException(Translation.MismatchingArguments.with(1, args.size()));
        long value = (long) Math.floor(args.getFirst().value());
        if (value < 2) return new LiteralValue((double) value);
        long a = 0;
        long b = 1;
        for (long i = 1; i < value; ++i) {
            long tmp = a + b;
            a = b;
            b = tmp;
        }
        return new LiteralValue((double) b);
    }

    public static LiteralValue random(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (args.size() > 2) throw new MathException(Translation.MismatchingArgumentsRange.with(0, 2, args.size()));
        double min = args.size() == 2 ? args.getFirst().value() : 0.0;
        double max = args.size() == 1 ? args.getFirst().value() : args.size() == 2 ? args.get(1).value() : 0.0;
        if (min >= max) throw new MathException(Translation.RandomMinGreaterEqMax.with(min, max));
        return new LiteralValue(ThreadLocalRandom.current().nextDouble(min, max));
    }

    public static LiteralValue randint(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (args.size() != 2) throw new MathException(Translation.MismatchingArguments.with(2, args.size()));
        long min = (long) Math.floor(args.get(0).value());
        long max = (long) Math.floor(args.get(1).value());
        if (min >= max) throw new MathException(Translation.RandomMinGreaterEqMax.with(min, max));
        return new LiteralValue((double) ThreadLocalRandom.current().nextLong(min, max));
    }

    public static LiteralValue min(CalcContext ctx, List<LiteralValue> args) {
        return new LiteralValue(args.stream().map(LiteralValue::value).reduce(0.0, Math::min));
    }

    public static LiteralValue max(CalcContext ctx, List<LiteralValue> args) {
        return new LiteralValue(args.stream().map(LiteralValue::value).reduce(0.0, Math::min));
    }

    public static LiteralValue toStack(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (args.size() != 1) throw new MathException(Translation.MismatchingArguments.with(1, args.size()));
        return new LiteralValue(args.getFirst().value() / 64.0);
    }

    public static LiteralValue toShulkerBox(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (args.size() != 1) throw new MathException(Translation.MismatchingArguments.with(1, args.size()));
        // 3 * 9 * 64
        return new LiteralValue(args.getFirst().value() / 1728.0);
    }

    public static LiteralValue fromStack(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (args.size() != 1) throw new MathException(Translation.MismatchingArguments.with(1, args.size()));
        return new LiteralValue(args.getFirst().value() * 64.0);
    }

    public static LiteralValue fromShulkerBox(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (args.size() != 1) throw new MathException(Translation.MismatchingArguments.with(1, args.size()));
        // 3 * 9 * 64
        return new LiteralValue(args.getFirst().value() * 1728.0);
    }

    public static LiteralValue x(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (!args.isEmpty()) throw new MathException(Translation.MismatchingArguments.with(0, args.size()));
        return new LiteralValue(ctx.playerPos().x());
    }

    public static LiteralValue y(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (!args.isEmpty()) throw new MathException(Translation.MismatchingArguments.with(0, args.size()));
        return new LiteralValue(ctx.playerPos().y());
    }

    public static LiteralValue z(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (!args.isEmpty()) throw new MathException(Translation.MismatchingArguments.with(0, args.size()));
        return new LiteralValue(ctx.playerPos().z());
    }

    public static LiteralValue time(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (!args.isEmpty()) throw new MathException(Translation.MismatchingArguments.with(0, args.size()));
        return new LiteralValue(ctx.time());
    }

    public static LiteralValue health(CalcContext ctx, List<LiteralValue> args) throws MathException {
        if (!args.isEmpty()) throw new MathException(Translation.MismatchingArguments.with(0, args.size()));
        return new LiteralValue(ctx.playerHealth());
    }
}
