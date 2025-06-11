package net.fishinghacks.utils.macros.exprs;

import net.fishinghacks.utils.macros.BreakoutException;
import net.fishinghacks.utils.macros.BreakoutException.EvalShouldStop;
import net.fishinghacks.utils.macros.EvalContext;
import net.fishinghacks.utils.macros.MacroException;
import net.fishinghacks.utils.macros.Translation;
import net.fishinghacks.utils.macros.parsing.Location;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record CallExpr(Expression function, List<Expression> arguments, Location location) implements Expression {
    @Override
    public LiteralValue eval(EvalContext context) throws MacroException, EvalShouldStop {
        var callable = function.eval(context);
        if (callable.type() != LiteralValue.ValueType.BuiltinFunction && callable.type() != LiteralValue.ValueType.Function)
            throw new MacroException(Translation.IsNotCallable.with(callable.type().getTranslatedName()), location);
        var args = new ArrayList<LiteralValue>();
        for (var arg : arguments) args.add(arg.eval(context));
        try {
            var builtin = callable.asBuiltinFunction();
            if (builtin.isPresent()) return builtin.get().function().call(context, args);
        } catch (MacroException e) {
            throw new MacroException(Translation.CalledFromNote.get(), location, e);
        }
        assert callable.asFunction().isPresent();
        var function = callable.asFunction().get();
        if (args.size() != function.arguments().size())
            throw new MacroException(Translation.MismatchingArguments.with(function.arguments().size(), args.size()),
                location);
        context.enterScope();
        var scope = context.scopes().getLast();
        for (int i = 0; i < args.size(); ++i) scope.put(function.arguments().get(i), args.get(i));
        try {
            for (var statement : function.statements()) statement.execute(context);
        } catch (MacroException e) {
            throw new MacroException(Translation.CalledFromNote.get(), location, e);
        } catch (BreakoutException.ReturnException e) {
            return e.returnValue;
        } catch (EvalShouldStop e) {
            throw e;
        } catch (BreakoutException e) {
            throw new MacroException(
                Component.literal("INTERNAL ERROR :: FUNCTION CALL RETURNED ILLEGAL BREAKOUT EXCEPTION"), location);
        } finally {
            context.exitScope();
        }
        return LiteralValue.NULL;
    }

    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(function.toString()).append("(");
        boolean notFirst = false;
        for (var expr : arguments) {
            if (notFirst) builder.append(", ");
            notFirst = true;
            builder.append(expr.toString());
        }
        return builder.append(")").toString();
    }
}