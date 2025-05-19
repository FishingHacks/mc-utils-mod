package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MathException;

public interface Expression {
    LiteralValue eval(EvalContext context) throws MathException, BreakoutException.EvalShouldStop;
    String toString();
}
