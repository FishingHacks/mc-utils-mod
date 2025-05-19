package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MathException;

public record OrExpr(Expression left, Expression right) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MathException, BreakoutException.EvalShouldStop {
        return new LiteralValue(left.eval(context).asBoolean() || right.eval(context).asBoolean());
    }

    @Override
    public String toString() {
        return left.toString() + " || " + right.toString();
    }
}
