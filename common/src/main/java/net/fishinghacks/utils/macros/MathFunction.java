package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.exprs.LiteralValue;

import java.util.List;

public interface MathFunction {
    LiteralValue call(EvalContext ctx, List<LiteralValue> args) throws MathException, BreakoutException.EvalShouldStop;
}
