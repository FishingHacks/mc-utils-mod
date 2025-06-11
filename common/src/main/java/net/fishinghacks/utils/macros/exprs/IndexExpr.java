package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;
import net.fishinghacks.utils.macros.Translation;
import net.fishinghacks.utils.macros.parsing.Location;
import org.jetbrains.annotations.NotNull;

public record IndexExpr(Expression left, Expression right, Location location) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MacroException, BreakoutException.EvalShouldStop {
        var leftEval = left.eval(context);
        switch (leftEval.type()) {
            case List -> {
                assert leftEval.asList().isPresent();
                var list = leftEval.asList().get();
                double rightIndex = right.eval(context).asDouble();
                if (rightIndex != Math.floor(rightIndex) || list.size() >= (int) rightIndex) return LiteralValue.NULL;
                return list.get((int) rightIndex);
            }
            case Map -> {
                assert leftEval.asMap().isPresent();
                var rightEval = right.eval(context);
                if (rightEval.type() != LiteralValue.ValueType.String) return LiteralValue.NULL;
                var val = leftEval.asMap().get().get(rightEval.asString());
                if (val == null) return LiteralValue.NULL;
                return val;
            }
            case String -> {
                double index = right.eval(context).asDouble();
                if (index != Math.floor(index) || leftEval.asString().length() >= (int) index) return LiteralValue.NULL;
                return new LiteralValue("" + leftEval.asString().charAt((int) index));
            }
            case Null -> {
                return LiteralValue.NULL;
            }
            default -> throw new MacroException(Translation.CannotIndexType.with(leftEval.type().getTranslatedName()),
                location);
        }
    }

    @Override
    public @NotNull String toString() {
        return left.toString() + "[" + right.toString() + "]";
    }
}
