package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;

import java.util.ArrayList;
import java.util.List;

public class ArrayExpression implements Expression {
    private final List<Expression> expressions;

    public ArrayExpression(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public LiteralValue eval(EvalContext context) throws MacroException, BreakoutException.EvalShouldStop {
        var list = new ArrayList<LiteralValue>(expressions.size());
        for(var expr : expressions) list.add(expr.eval(context));
        return new LiteralValue(list);
    }
}
