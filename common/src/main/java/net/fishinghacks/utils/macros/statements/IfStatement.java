package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MathException;
import net.fishinghacks.utils.macros.exprs.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IfStatement extends Statement {
    private final @Nullable List<Statement> elseBlock;
    private final List<IfEntry> entries;

    public IfStatement(@Nullable List<Statement> elseBlock, List<IfEntry> entries) {
        this.elseBlock = elseBlock;
        this.entries = entries;
    }

    @Override
    protected void run(EvalContext context) throws MathException, BreakoutException {
        for (var entry : entries) {
            if (!entry.expr.eval(context).asBoolean()) continue;

            context.enterScope();
            try {
                for (var statement : entry.statements) statement.execute(context);
            } finally {
                context.exitScope();
            }
            return;
        }
        if (elseBlock == null) return;

        context.enterScope();
        try {
            for (var statement : elseBlock) statement.execute(context);
        } finally {
            context.exitScope();
        }
    }

    public record IfEntry(Expression expr, List<Statement> statements) {
    }
}
