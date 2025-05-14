package net.fishinghacks.utils.client.calc;

import net.fishinghacks.utils.client.calc.exprs.Expression;
import net.fishinghacks.utils.client.calc.exprs.LiteralValue;

import java.util.HashMap;
import java.util.List;

public record CustomFunction(String source, Expression expr, List<String> variables) {
    public LiteralValue call(CalcContext context, List<LiteralValue> args) throws MathException {
        assert args.size() == variables.size();
        HashMap<String, LiteralValue> variables = new HashMap<>();
        for (int i = 0; i < args.size(); ++i) variables.put(this.variables.get(i), args.get(i));
        try {
            return expr.eval(new CalcContext(context.playerPos(), context.time(), context.playerHealth(), variables));
        } catch (MathException e) {
            e.source = source;
            throw e;
        }
    }
}
