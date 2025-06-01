package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.Whitelist;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class WhitelistCommand extends Command {
    public static final String LIST = "commands.whitelist.list";
    public static final Component LIST_EMPTY = Component.translatable("commands.whitelist.none");

    public WhitelistCommand() {
        super("whitelist");
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.then(literal("on").executes(this::enable)).then(literal("off").executes(this::disable))
            .then(literal("add").then(argument("target", StringArgumentType.string()).executes(this::addPlayer))).then(
                literal("remove").then(argument("target", StringArgumentType.string()).suggests(
                    (ctx, suggestionsBuilder) -> ctx.getSource() instanceof SharedSuggestionProvider && Minecraft.getInstance()
                        .isLocalServer() ? SharedSuggestionProvider.suggest(
                        Configs.whitelist.WHITELISTED_PLAYERS.get().stream().map(String::toString),
                        suggestionsBuilder) : Suggestions.empty()).executes(this::removePlayer)))
            .then(literal("list").executes(this::list));
    }

    protected int list(CommandContext<SharedSuggestionProvider> ctx) {
        if (commandIfRemote("whitelist list")) return SINGLE_SUCCESS;
        var list = Configs.whitelist.WHITELISTED_PLAYERS.get();
        if (list.isEmpty()) Minecraft.getInstance().getChatListener().handleSystemMessage(LIST_EMPTY, false);
        else Minecraft.getInstance().getChatListener()
            .handleSystemMessage(Component.translatable(LIST, String.valueOf(list.size()), String.join(", ", list)),
                false);

        return SINGLE_SUCCESS;
    }

    protected int disable(CommandContext<SharedSuggestionProvider> ctx) {
        if (commandIfRemote("whitelist off")) return SINGLE_SUCCESS;
        Whitelist.setEnabled(false);
        Minecraft.getInstance().getChatListener().handleSystemMessage(Translation.CmdWhitelistDisabled.get(), false);

        return SINGLE_SUCCESS;
    }

    protected int enable(CommandContext<SharedSuggestionProvider> ctx) {
        if (commandIfRemote("whitelist on")) return SINGLE_SUCCESS;
        Whitelist.setEnabled(true);
        Whitelist.kickUnlistedPlayers();
        Minecraft.getInstance().getChatListener().handleSystemMessage(Translation.CmdWhitelistEnabled.get(), false);

        return SINGLE_SUCCESS;
    }

    protected int addPlayer(CommandContext<SharedSuggestionProvider> ctx) {
        var player = ctx.getArgument("target", String.class);
        if (commandIfRemote("whitelist add " + player)) return SINGLE_SUCCESS;
        Whitelist.add(player);
        Minecraft.getInstance().getChatListener()
            .handleSystemMessage(Translation.CmdWhitelistAdded.with(player), false);

        return SINGLE_SUCCESS;
    }

    protected int removePlayer(CommandContext<SharedSuggestionProvider> ctx) {
        var player = ctx.getArgument("target", String.class);
        if (commandIfRemote("whitelist remove " + player)) return SINGLE_SUCCESS;
        Whitelist.remove(player);
        Minecraft.getInstance().getChatListener()
            .handleSystemMessage(Translation.CmdWhitelistRemoved.with(player), false);
        Whitelist.kickUnlistedPlayers();

        return SINGLE_SUCCESS;
    }

    protected boolean commandIfRemote(String command) {
        if (Minecraft.getInstance().isLocalServer()) return false;
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) conn.sendCommand(command);
        return true;
    }
}
