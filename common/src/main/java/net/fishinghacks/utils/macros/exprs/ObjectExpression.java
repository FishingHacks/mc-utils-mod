package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;

import java.util.HashMap;
import java.util.Map;

public class ObjectExpression implements Expression {
    private final Map<String, Expression> expressions;

    public ObjectExpression(Map<String, Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public LiteralValue eval(EvalContext context) throws MacroException, BreakoutException.EvalShouldStop {
        var map = new HashMap<String, LiteralValue>(expressions.size());
        for(var entry : expressions.entrySet()) map.put(entry.getKey(), entry.getValue().eval(context));
        return new LiteralValue(map);
    }
}
