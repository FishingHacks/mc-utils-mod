package net.fishinghacks.utils.commands;

import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.config.Configs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;

import java.util.HashMap;

public class CommandManager {
    static final HashMap<String, DotCommand> commands = new HashMap<>();

    static {
        register(new SayCommand(), new HelpCommand(), new ReloadCosmeticsCommand(), new ConfigCommand(),
            new CosmeticsCommand(), new InviteCommand(), new QuickCalcCommand(), new CalcCommand(), new MacroCommand());
    }

    public static void register(DotCommand... cmds) {
        for (DotCommand cmd : cmds) {
            CommandManager.commands.put(cmd.getName(), cmd);
        }
    }

    public static void run(String cmd, String args) {
        DotCommand command = commands.get(cmd);
        if (command == null) Minecraft.getInstance().getChatListener()
            .handleSystemMessage(Translation.CmdInvalid.with(cmd).withStyle(ChatFormatting.DARK_RED), false);
        else {
            try {
                command.run(args, Minecraft.getInstance().getChatListener());
            } catch (Exception e) {
                ChatListener listener = Minecraft.getInstance().getChatListener();
                listener.handleSystemMessage(Component.literal("Error: " + e.getMessage()), false);
                var el = e.getStackTrace()[0];
                if (el != null) listener.handleSystemMessage(Component.literal(
                        "At: " + el.getClassName() + "." + el.getMethodName() + "(" + el.getFileName() + ":" + el.getLineNumber() + ")"),
                    false);

                Throwable t = e.getCause();
                while (t != null) {
                    listener.handleSystemMessage(Component.literal("Caused by: " + e.getMessage()), false);
                    el = t.getStackTrace()[0];
                    if (el != null) listener.handleSystemMessage(Component.literal(
                            "At: " + el.getClassName() + "." + el.getMethodName() + "(" + el.getFileName() + ":" + el.getLineNumber() + ")"),
                        false);

                    t = t.getCause();
                }
            }
        }
    }

    public static boolean onChat(String message) {
        if (!message.startsWith(Configs.clientConfig.CMD_PREFIX.get()) || message.length() == 1) return false;
        String[] msg = message.substring(1).split(" ", 2);
        String cmd = msg[0];
        String args = "";
        if (msg.length > 1) {
            args = msg[1];
        }
        CommandManager.run(cmd, args);
        return true;
    }
}
