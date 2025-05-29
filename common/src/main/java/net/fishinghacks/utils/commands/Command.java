package net.fishinghacks.utils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public abstract class Command {
    protected static final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

    private final String name;

    protected Command(String name) {
        this.name = name;
    }

    protected abstract void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context);


    // Helper methods to painlessly infer the CommandSource generic type argument
    protected static <T> RequiredArgumentBuilder<SharedSuggestionProvider, T> argument(final String name,
                                                                            final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<SharedSuggestionProvider> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public void register(CommandDispatcher<SharedSuggestionProvider> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<SharedSuggestionProvider> builder = LiteralArgumentBuilder.literal(name);
        build(builder, context);
        dispatcher.register(builder);
    }

    public String getName() {
        return name;
    }
}
