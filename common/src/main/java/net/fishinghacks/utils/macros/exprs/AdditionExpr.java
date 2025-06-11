package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException.EvalShouldStop;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;
import org.jetbrains.annotations.NotNull;

public record AdditionExpr(Expression left, Expression right) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MacroException, EvalShouldStop {
        var leftEval = left.eval(context);
        if (leftEval.type() == LiteralValue.ValueType.String)
            return new LiteralValue(leftEval.asString() + right.eval(context).asString());
        return new LiteralValue(left.eval(context).asDouble() + right.eval(context).asDouble());
    }

    @Override
    public @NotNull String toString() {
        return left.toString() + " + " + right.toString();
    }
}
