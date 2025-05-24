package net.fishinghacks.utils.commands;

import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.macros.ExecutionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;

public class MacroCommand extends ArgDotCommand {
    public static void runMacro(String name) {
        var listener = Minecraft.getInstance().getChatListener();
        if (ExecutionManager.startMacro(name, component -> listener.handleSystemMessage(component, false),
            index -> listener.handleSystemMessage(Translation.CmdMacrosStartedRunning.with(index, index), false),
            (index, macro) -> listener.handleSystemMessage(
                Translation.CmdMacrosExited.with(index, macro != null ? macro.name() : ""), false)).isPresent()) return;
        listener.handleSystemMessage(Translation.CmdMacrosFailedToStart.with(name), false);
    }

    @Override
    public void run(String[] args, ChatListener listener) {
        switch (args.length > 0 ? args[0] : null) {
            case "list" -> {
                var runningMacros = ExecutionManager.getRunningMacros();
                if (runningMacros.isEmpty()) {
                    listener.handleSystemMessage(Translation.CmdMacrosNoneRunning.get(), false);
                    return;
                }
                listener.handleSystemMessage(Translation.CmdMacrosRunning.get(), false);
                for (var entry : runningMacros)
                    listener.handleSystemMessage(
                        Component.literal("#" + entry.getKey() + " ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(entry.getValue().name()).withStyle(ChatFormatting.WHITE)), false);
            }
            case "start" -> {
                if (args.length < 2) {
                    listener.handleSystemMessage(Translation.CmdMacrosStartUsage.get(), false);
                    return;
                }
                StringBuilder file = new StringBuilder();
                for (int i = 1; i < args.length; ++i) {
                    if (i != 1) file.append(" ");
                    file.append(args[i]);
                }
                runMacro(file.toString());
            }
            case "stop" -> {
                if (args.length < 2) {
                    listener.handleSystemMessage(Translation.CmdMacrosStopUsage.get(), false);
                    return;
                }
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    listener.handleSystemMessage(Translation.CmdMacrosStopUsage.get(), false);
                    return;
                }
                listener.handleSystemMessage(Translation.CmdMacrosStopped.with(id), false);
            }
            case null, default -> listener.handleSystemMessage(Translation.CmdMacrosHelp.get(), false);
        }
    }

    @Override
    public String getName() {
        return "macros";
    }
}
