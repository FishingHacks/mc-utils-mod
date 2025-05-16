package net.fishinghacks.utils.calc.exprs;

import net.fishinghacks.utils.calc.CalcContext;
import net.fishinghacks.utils.calc.MathException;

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
