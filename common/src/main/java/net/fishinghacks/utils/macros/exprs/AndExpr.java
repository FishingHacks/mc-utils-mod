package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;
import org.jetbrains.annotations.NotNull;

public record AndExpr(Expression left, Expression right) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MacroException, BreakoutException.EvalShouldStop {
        return new LiteralValue(left.eval(context).asBoolean() && right.eval(context).asBoolean());
    }

    @Override
    public @NotNull String toString() {
        return left.toString() + " && " + right.toString();
    }
}
