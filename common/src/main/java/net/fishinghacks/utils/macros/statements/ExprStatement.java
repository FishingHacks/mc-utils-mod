package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;
import net.fishinghacks.utils.macros.exprs.Expression;

public class ExprStatement extends Statement {
    public final Expression expr;

    public ExprStatement(Expression expr) {
        this.expr = expr;
    }

    @Override
    protected void run(EvalContext context) throws MacroException, BreakoutException {
        expr.eval(context);
    }
}
