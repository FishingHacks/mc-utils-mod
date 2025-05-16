package net.fishinghacks.utils.calc;

import net.fishinghacks.utils.calc.exprs.LiteralValue;

import java.util.List;

public interface MathFunction {
    LiteralValue call(CalcContext ctx, List<LiteralValue> args) throws MathException;
}
