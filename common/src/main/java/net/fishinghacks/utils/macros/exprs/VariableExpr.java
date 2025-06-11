package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.*;
import net.fishinghacks.utils.macros.parsing.Location;
import org.jetbrains.annotations.NotNull;

public record VariableExpr(String name, Location location) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MacroException {
        var variable = context.lookup(name);
        if (variable.isEmpty())
            throw new MacroException(Translation.UnboundVariable.with(name), location);
        return variable.get();
    }

    @Override
    public @NotNull String toString() {
        return name;
    }
}
