package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;

public class BreakStatement extends Statement {
    @Override
    protected void run(EvalContext context) throws BreakoutException {
        throw new BreakoutException.BreakException();
    }
}
