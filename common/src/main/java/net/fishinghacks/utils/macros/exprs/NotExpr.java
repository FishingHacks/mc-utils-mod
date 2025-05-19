package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MathException;

public record NotExpr(Expression e) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MathException, BreakoutException.EvalShouldStop {
        return new LiteralValue(!e.eval(context).asBoolean());
    }

    @Override
    public String toString() {
        return "!" + e.toString();
    }
}
