package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.cosmetics.CosmeticsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public class CosmeticsCommand extends Command {
    public CosmeticsCommand() {
        super("cosmetics");
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.executes(ignored -> {
            if (ClientConnectionHandler.getInstance().isConnected())
                Minecraft.getInstance().setScreen(new CosmeticsScreen(null));
            return SINGLE_SUCCESS;
        });
    }
}
