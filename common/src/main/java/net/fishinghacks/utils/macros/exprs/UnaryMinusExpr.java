package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;
import org.jetbrains.annotations.NotNull;

public record UnaryMinusExpr(Expression expr) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MacroException, BreakoutException.EvalShouldStop {
        return new LiteralValue(-expr.eval(context).asDouble());
    }

    @Override
    public @NotNull String toString() {
        return "-" + expr.toString();
    }
}
