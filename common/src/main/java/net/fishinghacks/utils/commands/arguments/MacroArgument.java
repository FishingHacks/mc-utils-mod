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
import java.util.concurrent.CompletableFuture;

public class MacroArgument implements ArgumentType<String> {
    public static final MacroArgument INSTANCE = new MacroArgument();
    private static final Collection<String> EXAMPLES = List.of("My Fancy Macro", "My Other Macro");

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String text = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        return text;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof SharedSuggestionProvider)) return Suggestions.empty();
        var macros = ExecutionManager.getMacros();
        if (macros.isEmpty()) return Suggestions.empty();
        return SharedSuggestionProvider.suggest(macros.stream(), builder);
    }

    protected MacroArgument() {
    }
}
