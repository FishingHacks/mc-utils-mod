package net.fishinghacks.utils.client.commands;

import net.fishinghacks.utils.client.E4MCStore;
import net.fishinghacks.utils.client.connection.ClientConnectionHandler;
import net.fishinghacks.utils.client.gui.PauseMenuScreen;
import net.fishinghacks.utils.common.Translation;
import net.fishinghacks.utils.common.connection.Connection;
import net.fishinghacks.utils.common.connection.packets.InvitePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.neoforged.fml.ModList;

public class InviteCommand extends DotCommand {
    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public void run(String args, ChatListener listener) {
        if (!ModList.get().isLoaded("e4mc_minecraft")) error(listener, Translation.CmdInviteE4MCMissing);
        else if (!E4MCStore.hasLink() || E4MCStore.getLink() == null) error(listener, Translation.CmdInviteNotShared);
        else if (!ClientConnectionHandler.getInstance().isConnected() || ClientConnectionHandler.getInstance()
            .getConnection() == null) error(listener, Translation.CmdInviteNotConnected);
        else {
            String name = args.trim();
            if (name.isEmpty()) PauseMenuScreen.invitePlayer(null);
            else {
                String link = E4MCStore.getLink();
                Connection conn = ClientConnectionHandler.getInstance().getConnection();
                if (link == null || conn == null) return;
                conn.send(new InvitePacket(link, name));
            }
        }
    }

    private static void error(ChatListener listener, Translation err) {
        listener.handleSystemMessage(err.get().copy().withStyle(ChatFormatting.RED), false);
    }
}
