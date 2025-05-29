package net.fishinghacks.utils.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fishinghacks.utils.macros.ExecutionManager;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RunningMacroArgument implements ArgumentType<Integer> {
    public static final RunningMacroArgument INSTANCE = new RunningMacroArgument();
    private static final Collection<String> EXAMPLES = List.of("1", "#3");

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        if (reader.peek() == '#') reader.read();
        return reader.readInt();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggest(
            ExecutionManager.getRunningMacros().stream().map(Map.Entry::getKey).map(String::valueOf),
            builder) : Suggestions.empty();
    }

    protected RunningMacroArgument() {
    }
}
