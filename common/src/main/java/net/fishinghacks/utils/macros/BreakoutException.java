package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.exprs.LiteralValue;

public abstract class BreakoutException extends Throwable {
    public static class BreakException extends BreakoutException {
    }

    public static class ContinueException extends BreakoutException {
    }

    public static class EvalShouldStop extends BreakoutException {
    }

    public static class ReturnException extends BreakoutException {
        public final LiteralValue returnValue;

        public ReturnException(LiteralValue returnValue) {
            this.returnValue = returnValue;
        }
    }
}
