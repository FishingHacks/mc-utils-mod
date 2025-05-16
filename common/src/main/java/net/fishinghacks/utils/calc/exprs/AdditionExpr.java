package net.fishinghacks.utils.calc.exprs;

import net.fishinghacks.utils.calc.CalcContext;
import net.fishinghacks.utils.calc.MathException;

public record AdditionExpr(Expression left, Expression right) implements Expression {
    @Override
    public LiteralValue eval(CalcContext context) throws MathException {
        return new LiteralValue(left.eval(context).value() + right.eval(context).value());
    }

    @Override
    public String toString() {
        return left.toString() + " + " + right.toString();
    }
}
