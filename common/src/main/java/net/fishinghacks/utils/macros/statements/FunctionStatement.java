package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.FunctionValue;
import net.fishinghacks.utils.macros.exprs.LiteralValue;

public class FunctionStatement extends Statement {
    public final String name;
    public final FunctionValue value;

    public FunctionStatement(String name, FunctionValue value) {
        this.name = name;
        this.value = value;
    }

    @Override
    protected void run(EvalContext context) {
        context.scopes().getLast().put(name, new LiteralValue(value));
    }
}
