package net.fishinghacks.utils.macros.statements;

import net.fishinghacks.utils.macros.*;
import net.fishinghacks.utils.macros.exprs.Expression;
import net.fishinghacks.utils.macros.exprs.LiteralValue;
import net.fishinghacks.utils.macros.parsing.Location;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ForStatement extends Statement {
    private final Expression expression;
    private final List<Statement> statements;
    private final String inKey;
    private final Location loc;

    public ForStatement(Expression expression, List<Statement> statements, String inKey, Location loc) {
        this.expression = expression;
        this.statements = statements;
        this.inKey = inKey;
        this.loc = loc;
    }

    @Override
    protected void run(EvalContext context) throws MacroException, BreakoutException {
        var literalValue = expression.eval(context);
        Stream<LiteralValue> stream;
        switch (literalValue.type()) {
            case Number -> stream = IntStream.range(0, ((int) Math.floor(literalValue.asDouble())) - 1)
                .mapToObj(LiteralValue::new);
            case String -> stream = literalValue.asString().chars().mapToObj(v -> new LiteralValue("" + (char) v));
            case List -> {
                assert literalValue.asList().isPresent();
                stream = literalValue.asList().get().stream();
            }
            default ->
                throw new MacroException(Translation.CannotIndexType.with(literalValue.type().getTranslatedName()), loc);
        }
        var iterator = stream.iterator();
        while (iterator.hasNext()) {
            Executor.checkExit(context);
            context.forLoopScope(inKey, iterator.next());
            try {
                for (var statement : statements) statement.execute(context);
            } catch (BreakoutException e) {
                if (e instanceof BreakoutException.BreakException) break;
                else if (!(e instanceof BreakoutException.ContinueException)) throw e;
            } finally {
                context.exitScope();
            }
        }
    }
}
