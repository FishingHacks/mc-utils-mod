package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fishinghacks.utils.commands.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public class QuickCalcCommand extends Command {
    public QuickCalcCommand() {
        super("=");
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.then(argument("expression", StringArgumentType.greedyString()).executes(ctx -> {
            CalcCommand.runCalculation(ctx.getArgument("expression", String.class),
                Minecraft.getInstance().getChatListener());
            return SINGLE_SUCCESS;
        }));
    }
}
