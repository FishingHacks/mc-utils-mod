package net.fishinghacks.utils.calc.exprs;

import net.fishinghacks.utils.calc.CalcContext;
import net.fishinghacks.utils.calc.MathException;

public interface Expression {
    LiteralValue eval(CalcContext context) throws MathException;
    String toString();
}
