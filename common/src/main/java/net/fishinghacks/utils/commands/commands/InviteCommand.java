package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fishinghacks.utils.E4MCStore;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.connection.Connection;
import net.fishinghacks.utils.gui.PauseMenuScreen;
import net.fishinghacks.utils.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Nullable;

public class InviteCommand extends Command {
    public InviteCommand() {
        super("invite");
    }

    public int run(@Nullable String player) {
        var listener = Minecraft.getInstance().getChatListener();
        if (!Services.PLATFORM.isModLoaded("e4mc_minecraft")) error(listener, Translation.CmdInviteE4MCMissing);
        else if (!E4MCStore.hasLink() || E4MCStore.getLink() == null) error(listener, Translation.CmdInviteNotShared);
        else if (!ClientConnectionHandler.getInstance().isConnected() || ClientConnectionHandler.getInstance()
            .getConnection() == null) error(listener, Translation.CmdInviteNotConnected);
        else {
            if (player == null || player.isEmpty()) PauseMenuScreen.invitePlayer(null);
            else {
                String link = E4MCStore.getLink();
                Connection conn = ClientConnectionHandler.getInstance().getConnection();
                if (link == null || conn == null) return SINGLE_SUCCESS;
                PauseMenuScreen.sendInvite(conn, link, player);
            }
        }
        return SINGLE_SUCCESS;
    }

    private static void error(ChatListener listener, Translation err) {
        listener.handleSystemMessage(err.get().copy().withStyle(ChatFormatting.RED), false);
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.executes(ignored -> run(null)).then(argument("player", StringArgumentType.string()).executes(
            ctx -> run(ctx.getArgument("player", String.class))));
    }
}
