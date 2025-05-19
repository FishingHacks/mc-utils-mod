package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MathException;

public class BreakStatement extends Statement {
    @Override
    protected void run(EvalContext context) throws MathException, BreakoutException {
        throw new BreakoutException.BreakException();
    }
}
