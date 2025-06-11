package net.fishinghacks.utils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fishinghacks.utils.ModDisabler;
import net.fishinghacks.utils.commands.commands.*;
import net.fishinghacks.utils.config.Configs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private static CommandDispatcher<SharedSuggestionProvider> DISPATCHER = null;
    private static CommandDispatcher<SharedSuggestionProvider> DISABLED_DISPATCHER = null;
    private static List<Command> COMMANDS = null;

    public static void init() {
        getDispatcher();
    }

    public static CommandDispatcher<SharedSuggestionProvider> getDispatcher() {
        if (DISPATCHER == null) onLoad();
        return ModDisabler.isModDisabled() ? DISABLED_DISPATCHER : DISPATCHER;
    }

    public static List<Command> getCommands() {
        if (COMMANDS == null) onLoad();
        return COMMANDS;
    }

    private static void onLoad() {
        DISPATCHER = new CommandDispatcher<>();
        DISABLED_DISPATCHER = new CommandDispatcher<>();
        COMMANDS = new ArrayList<>();
        CommandBuildContext context = Commands.createValidationContext(VanillaRegistries.createLookup());
        register(new CalcCommand(), context);
        register(new ConfigCommand(), context);
        register(new CosmeticsCommand(), context);
        register(new HelpCommand(), context);
        register(new InviteCommand(), context);
        register(new MacroCommand(), context);
        register(new QuickCalcCommand(), context);
        register(new ReloadCosmeticsCommand(), context);
        register(new WhitelistCommand(), context);
        register(new MufflerCommand(), context);
        registerDisabled(new EnableModCommand(), context);
    }

    public static void registerDisabled(Command command, CommandBuildContext buildContext) {
        command.register(DISABLED_DISPATCHER, buildContext);
    }

    public static void register(Command command, CommandBuildContext buildContext) {
        COMMANDS.removeIf(c -> c.getName().equals(command.getName()));
        command.register(getDispatcher(), buildContext);
        COMMANDS.add(command);
    }

    public static boolean onChat(String message) {
        if (!message.startsWith(Configs.clientConfig.CMD_PREFIX.get()) || message.length() == 1) return false;
        try {
            dispatch(message.substring(1));
        } catch (CommandSyntaxException e) {
            Minecraft.getInstance().getChatListener().handleSystemMessage(
                Component.empty().append(ComponentUtils.fromMessage(e.getRawMessage())).withStyle(ChatFormatting.RED),
                false);
            var context = e.getContext();
            if (context != null) {
                var content = context.substring(0, context.length() - 9);
                Minecraft.getInstance().getChatListener().handleSystemMessage(
                    Component.literal(content).withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" <--[HERE]").withStyle(ChatFormatting.RED))
                        .withStyle(s -> s.withClickEvent(new ClickEvent.SuggestCommand(message))), false);
            }
        }
        return true;
    }

    public static void dispatch(String message) throws CommandSyntaxException {
        if (Minecraft.getInstance().player == null) return;
        SharedSuggestionProvider provider = Minecraft.getInstance().player.connection.getSuggestionsProvider();
        getDispatcher().execute(message, provider);
    }
}
