package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;

public record EqExpr(Expression left, Expression right) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MacroException, BreakoutException.EvalShouldStop {
        return new LiteralValue(left.eval(context).equal(right.eval(context)));
    }

    @Override
    public String toString() {
        return left.toString() + " == " + right.toString();
    }
}
