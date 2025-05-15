package net.fishinghacks.utils.client.commands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;

public class SayCommand extends DotCommand {
    @Override
    public String getName() {
        return "say";
    }

    @Override
    public void run(String args, ChatListener listener) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        player.connection.sendChat(args);
    }
}
