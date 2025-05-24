package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;
import net.fishinghacks.utils.macros.exprs.Expression;
import net.fishinghacks.utils.macros.exprs.LiteralValue;
import org.jetbrains.annotations.Nullable;

public class ReturnStatement extends Statement {
    private final @Nullable Expression returnExpr;

    public ReturnStatement(@Nullable Expression returnExpr) {
        this.returnExpr = returnExpr;
    }

    @Override
    protected void run(EvalContext context) throws MacroException, BreakoutException {
        if (returnExpr == null) throw new BreakoutException.ReturnException(LiteralValue.NULL);
        throw new BreakoutException.ReturnException(returnExpr.eval(context));
    }
}
