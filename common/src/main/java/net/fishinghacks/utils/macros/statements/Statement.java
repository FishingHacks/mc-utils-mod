package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MathException;

public abstract class Statement {
    protected abstract void run(EvalContext context) throws MathException, BreakoutException;

    public final void execute(EvalContext context) throws BreakoutException, MathException {
        if(context.shouldStop().get()) throw new BreakoutException.EvalShouldStop();
        run(context);
    }
}
