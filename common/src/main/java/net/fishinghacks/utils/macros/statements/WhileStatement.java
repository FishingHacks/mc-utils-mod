package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.Executor;
import net.fishinghacks.utils.macros.MathException;
import net.fishinghacks.utils.macros.exprs.Expression;

import java.util.List;

public class WhileStatement extends Statement {
    private final Expression condition;
    private final List<Statement> body;

    public WhileStatement(Expression condition, List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void run(EvalContext context) throws MathException, BreakoutException {
        while (condition.eval(context).asBoolean()) {
            Executor.checkExit(context);
            context.enterScope();
            try {
                for (var statement : body) statement.execute(context);
            } finally {
                context.exitScope();
            }
        }
    }
}
