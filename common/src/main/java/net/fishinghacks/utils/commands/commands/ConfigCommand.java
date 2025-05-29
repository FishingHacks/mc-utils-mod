package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.gui.configuration.ConfigSectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config");
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.executes(ignored -> {
            ConfigSectionScreen.open(Minecraft.getInstance(), Configs.clientConfig);
            return SINGLE_SUCCESS;
        });
    }
}
