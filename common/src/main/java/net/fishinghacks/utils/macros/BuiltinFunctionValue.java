package net.fishinghacks.utils.macros;

public record BuiltinFunctionValue(String name, NativeFunction function) {
    public static BuiltinFunctionValue withShouldExitCheck(String name, NativeFunction function) {
        return new BuiltinFunctionValue(name, (context, values) -> {
            if (context.shouldStop().get()) throw new BreakoutException.EvalShouldStop();
            return function.call(context, values);
        });
    }
}
