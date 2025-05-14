package net.fishinghacks.utils.client.calc;

import net.fishinghacks.utils.client.calc.exprs.LiteralValue;

import java.util.List;

public interface MathFunction {
    LiteralValue call(CalcContext ctx, List<LiteralValue> args) throws MathException;
}
