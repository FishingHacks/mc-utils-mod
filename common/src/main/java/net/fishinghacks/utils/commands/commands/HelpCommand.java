package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.commands.CommandManager;
import net.fishinghacks.utils.commands.arguments.CommandArgument;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    private int help_command(String name) {
        if (I18n.exists("utils.cmd." + name + ".help")) {
            for (String line : I18n.get("utils.cmd." + name + ".help").split("\n")) {
                Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(line), false);
            }
        } else Minecraft.getInstance().getChatListener()
            .handleSystemMessage(Component.literal(".").append(name).withStyle(ChatFormatting.YELLOW), false);
        return SINGLE_SUCCESS;
    }

    private int help() {
        CommandManager.getCommands().forEach(cmd -> help_command(cmd.getName()));
        return SINGLE_SUCCESS;
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.executes(ignored -> help()).then(argument("command", CommandArgument.INSTANCE).executes(
            ctx -> help_command(ctx.getArgument("command", String.class))));
    }
}
