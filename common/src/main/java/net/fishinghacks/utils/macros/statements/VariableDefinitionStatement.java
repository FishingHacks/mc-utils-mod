package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;
import net.fishinghacks.utils.macros.exprs.Expression;

public class VariableDefinitionStatement extends Statement {
    private final String name;
    private final Expression expr;

    public VariableDefinitionStatement(String name, Expression expr) {
        this.name = name;
        this.expr = expr;
    }

    @Override
    protected void run(EvalContext context) throws MacroException, BreakoutException {
        context.scopes().getLast().put(name, expr.eval(context));
    }
}
