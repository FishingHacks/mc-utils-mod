package net.fishinghacks.utils.client.calc.exprs;

import net.fishinghacks.utils.client.calc.*;

public record VariableExpr(String name, int characterPos) implements Expression {
    @Override
    public LiteralValue eval(CalcContext context) throws MathException {
        LiteralValue builtinVariable = CalcContext.builtinVariables.get(name);
        LiteralValue localVariable = context.localVariables().get(name);
        if (localVariable == null && builtinVariable == null)
            throw new MathException(Translation.UnboundVariable.with(name), characterPos, "");
        return builtinVariable == null ? localVariable : builtinVariable;
    }

    @Override
    public String toString() {
        return name;
    }
}
