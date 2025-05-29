package net.fishinghacks.utils.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.commands.CommandManager;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandArgument implements ArgumentType<String> {
    public static final CommandArgument INSTANCE = new CommandArgument();
    private static final Collection<String> EXAMPLES = List.of("help", "config", "calc");

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggest(
            CommandManager.getCommands().stream().map(Command::getName), builder) : Suggestions.empty();
    }

    protected CommandArgument() {
    }
}
