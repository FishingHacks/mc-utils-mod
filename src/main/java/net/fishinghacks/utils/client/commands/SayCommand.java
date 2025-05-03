package net.fishinghacks.utils.client.commands;
import net.fishinghacks.utils.common.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class SayCommand extends DotCommand {
    @Override
    public String getName() {
        return "say";
    }

    @Override
    public @Nullable Component description() {
        return Translation.CmdSayHelp.get();
    }

    @Override
    public void run(String args, ChatListener listener) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        player.connection.sendChat(args);
    }
}
