package net.fishinghacks.utils.client.calc.exprs;

import net.fishinghacks.utils.client.calc.CalcContext;
import net.fishinghacks.utils.client.calc.MathException;

public interface Expression {
    LiteralValue eval(CalcContext context) throws MathException;
    String toString();
}
