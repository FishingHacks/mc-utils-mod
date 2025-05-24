package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;

public abstract class Statement {
    protected abstract void run(EvalContext context) throws MacroException, BreakoutException;

    public final void execute(EvalContext context) throws BreakoutException, MacroException {
        if(context.shouldStop().get()) throw new BreakoutException.EvalShouldStop();
        run(context);
    }
}
