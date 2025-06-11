package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fishinghacks.utils.ModDisabler;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.commands.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public class EnableModCommand extends Command {
    public EnableModCommand() {
        super("enable_mod");
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.executes(ignored -> {
            ModDisabler.enable();
            Minecraft.getInstance().getChatListener().handleSystemMessage(Translation.CmdEnableModEnabled.get(), false);
            return SINGLE_SUCCESS;
        });
    }
}
