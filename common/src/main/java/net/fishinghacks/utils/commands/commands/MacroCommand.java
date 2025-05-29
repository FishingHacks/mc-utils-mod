package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.commands.arguments.MacroArgument;
import net.fishinghacks.utils.commands.arguments.RunningMacroArgument;
import net.fishinghacks.utils.macros.ExecutionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class MacroCommand extends Command {
    public MacroCommand() {
        super("macros");
    }

    public static void runMacro(String name) {
        var listener = Minecraft.getInstance().getChatListener();
        if (ExecutionManager.startMacro(name, component -> listener.handleSystemMessage(component, false),
            index -> listener.handleSystemMessage(Translation.CmdMacrosStartedRunning.with(index, index), false),
            (index, macro) -> listener.handleSystemMessage(
                Translation.CmdMacrosExited.with(index, macro != null ? macro.name() : ""), false)).isPresent()) return;
        listener.handleSystemMessage(Translation.CmdMacrosFailedToStart.with(name), false);
    }

    public int list(CommandContext<SharedSuggestionProvider> ctx) {
        var listener = Minecraft.getInstance().getChatListener();
        var runningMacros = ExecutionManager.getRunningMacros();
        if (runningMacros.isEmpty()) {
            listener.handleSystemMessage(Translation.CmdMacrosNoneRunning.get(), false);
            return SINGLE_SUCCESS;
        }
        listener.handleSystemMessage(Translation.CmdMacrosRunning.get(), false);
        for (var entry : runningMacros)
            listener.handleSystemMessage(Component.literal("#" + entry.getKey() + " ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(entry.getValue().name()).withStyle(ChatFormatting.WHITE)), false);

        return SINGLE_SUCCESS;
    }

    public int start(CommandContext<SharedSuggestionProvider> ctx) {
        runMacro(ctx.getArgument("macro", String.class));
        return SINGLE_SUCCESS;
    }

    public int stop(CommandContext<SharedSuggestionProvider> ctx) {
        int id = ctx.getArgument("id", Integer.class);
        ExecutionManager.stopMacro(id);
        Minecraft.getInstance().getChatListener().handleSystemMessage(Translation.CmdMacrosStopped.with(id), false);
        return SINGLE_SUCCESS;
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.then(literal("list").executes(this::list))
            .then(literal("start").then(argument("macro", MacroArgument.INSTANCE).executes(this::start)))
            .then(literal("stop").then(argument("id", RunningMacroArgument.INSTANCE).executes(this::stop)));
    }
}
