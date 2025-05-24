package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;

public interface Expression {
    LiteralValue eval(EvalContext context) throws MacroException, BreakoutException.EvalShouldStop;
    String toString();
}
