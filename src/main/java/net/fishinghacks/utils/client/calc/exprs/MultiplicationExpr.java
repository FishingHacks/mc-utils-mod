package net.fishinghacks.utils.client.calc.exprs;

import net.fishinghacks.utils.client.calc.CalcContext;
import net.fishinghacks.utils.client.calc.MathException;

public record MultiplicationExpr(Expression left, Expression right) implements Expression {
    @Override
    public LiteralValue eval(CalcContext context) throws MathException {
        return new LiteralValue(left.eval(context).value() * right.eval(context).value());
    }

    @Override
    public String toString() {
        return left.toString() + " * " + right.toString();
    }
}
