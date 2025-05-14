package net.fishinghacks.utils.client.calc.exprs;

import net.fishinghacks.utils.client.calc.CalcContext;
import net.fishinghacks.utils.client.calc.MathException;

public record UnaryMinusExpr(Expression expr) implements Expression {
    @Override
    public LiteralValue eval(CalcContext context) throws MathException {
        return new LiteralValue(-expr.eval(context).value());
    }

    @Override
    public String toString() {
        return "-" + expr.toString();
    }
}
