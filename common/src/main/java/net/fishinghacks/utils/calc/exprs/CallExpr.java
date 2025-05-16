package net.fishinghacks.utils.calc.exprs;

import net.fishinghacks.utils.calc.CalcContext;
import net.fishinghacks.utils.calc.MathException;
import net.fishinghacks.utils.calc.Translation;

import java.util.ArrayList;
import java.util.List;

public record CallExpr(String name, List<Expression> arguments, int charPos) implements Expression {
    @Override
    public LiteralValue eval(CalcContext context) throws MathException {
        var func = CalcContext.builtinFunctions.get(name);
        var customFunc = CalcContext.customFunctions.get(name);
        if (func != null) try {
            List<LiteralValue> evaluated = new ArrayList<>();
            for (Expression expr : arguments) evaluated.add(expr.eval(context));
            return func.call(context, evaluated);
        } catch (MathException e) {
            throw e.withCharPos(charPos, "");
        }
        else if (customFunc != null) {
            if (arguments.size() != customFunc.variables().size()) throw new MathException(
                Translation.MismatchingArguments.with(customFunc.variables().size(), arguments.size()));
            List<LiteralValue> evaluated = new ArrayList<>();
            for (Expression expr : arguments) evaluated.add(expr.eval(context));
            return customFunc.call(context, evaluated);
        } else throw new MathException(Translation.UnboundFunction.with(name), charPos, "");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append("(");
        boolean notFirst = false;
        for (var expr : arguments) {
            if (notFirst) builder.append(", ");
            notFirst = true;
            builder.append(expr.toString());
        }
        builder.append(")");
        return builder.toString();
    }
}
