package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;
import org.jetbrains.annotations.NotNull;

public record InlineIfExpr(Expression determiner, Expression left, Expression right) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MacroException, BreakoutException.EvalShouldStop {
        return determiner.eval(context).asBoolean() ? left.eval(context) : right.eval(context);
    }

    @Override
    public @NotNull String toString() {
        return determiner.toString() + "? " + left.toString() + " : " + right.toString();
    }
}
