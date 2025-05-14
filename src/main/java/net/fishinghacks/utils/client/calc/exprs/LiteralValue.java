package net.fishinghacks.utils.client.calc.exprs;

import net.fishinghacks.utils.client.calc.CalcContext;

public record LiteralValue(double value) implements Expression {

    @Override
    public LiteralValue eval(CalcContext context) {
        return this;
    }

    @Override
    public String toString() {
        return doubleToString(value);
    }

    public static String doubleToString(double value) {
        return value == Math.floor(value) ? "" + (long) Math.floor(value) : "" + value;
    }
}