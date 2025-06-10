package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.config.Configs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MufflerCommand extends Command {
    public MufflerCommand() {
        super("muffler");
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.then(literal("set-volume").then(argument("sound", ResourceLocationArgument.id()).suggests(
                (ctx, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(ctx.getSource().getAvailableSounds(),
                    suggestionsBuilder))
            .then(argument("volume", IntegerArgumentType.integer(0, 100)).executes(this::setVolume)))).then(
            literal("get-volume").then(argument("sound", ResourceLocationArgument.id()).suggests(
                (ctx, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(
                    ctx.getSource().getAvailableSounds(), suggestionsBuilder)).executes(this::getVolume))).then(
            literal("mute").then(argument("sound", ResourceLocationArgument.id()).suggests(
                (ctx, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(
                    ctx.getSource().getAvailableSounds(), suggestionsBuilder)).executes(this::mute))).then(
            literal("unmute").then(argument("sound", ResourceLocationArgument.id()).suggests(
                (ctx, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(
                    ctx.getSource().getAvailableSounds(), suggestionsBuilder)).executes(this::unmute)));
    }

    protected int getVolume(CommandContext<SharedSuggestionProvider> ctx) {
        var sound = ctx.getArgument("sound", ResourceLocation.class);
        Integer volume = Configs.clientConfig.MUFFLER_STATE.get().get(sound);
        if (volume == null) volume = 100;
        if (volume < 0) Minecraft.getInstance().getChatListener().handleSystemMessage(
            Translation.CmdMufflerGetVolumeMuted.with(
                Component.literal(sound.toString()).withStyle(ChatFormatting.AQUA),
                Component.literal((-volume) + "%").withStyle(ChatFormatting.GREEN)), false);
        else Minecraft.getInstance().getChatListener().handleSystemMessage(
            Translation.CmdMufflerGetVolume.with(Component.literal(sound.toString()).withStyle(ChatFormatting.AQUA),
                Component.literal(volume + "%").withStyle(ChatFormatting.GREEN)), false);


        return SINGLE_SUCCESS;
    }

    protected int setVolume(CommandContext<SharedSuggestionProvider> ctx) {
        var sound = ctx.getArgument("sound", ResourceLocation.class);
        var volume = Math.clamp(ctx.getArgument("volume", Integer.class), 0, 100);
        Configs.clientConfig.MUFFLER_STATE.get().put(sound, volume);
        Configs.clientConfig.MUFFLER_STATE.save();
        Minecraft.getInstance().getChatListener().handleSystemMessage(
            Translation.CmdMufflerSetVolume.with(Component.literal(sound.toString()).withStyle(ChatFormatting.AQUA),
                Component.literal(volume + "%").withStyle(ChatFormatting.GREEN)), false);

        return SINGLE_SUCCESS;
    }

    protected int mute(CommandContext<SharedSuggestionProvider> ctx) {
        var sound = ctx.getArgument("sound", ResourceLocation.class);
        var state = Configs.clientConfig.MUFFLER_STATE.get();
        var volume = state.get(sound);
        if (volume == null) volume = -100;
        else if (volume > 0) volume = -volume;
        state.put(sound, volume);
        Configs.clientConfig.MUFFLER_STATE.save();

        Minecraft.getInstance().getChatListener().handleSystemMessage(
            Translation.CmdMufflerMuted.with(Component.literal(sound.toString()).withStyle(ChatFormatting.AQUA)),
            false);

        return SINGLE_SUCCESS;
    }

    protected int unmute(CommandContext<SharedSuggestionProvider> ctx) {
        var sound = ctx.getArgument("sound", ResourceLocation.class);
        var state = Configs.clientConfig.MUFFLER_STATE.get();
        var volume = state.get(sound);
        if (volume < 0) {
            state.put(sound, -volume);
            Configs.clientConfig.MUFFLER_STATE.save();
        }
        Minecraft.getInstance().getChatListener().handleSystemMessage(
            Translation.CmdMufflerUnmuted.with(Component.literal(sound.toString()).withStyle(ChatFormatting.AQUA)),
            false);

        return SINGLE_SUCCESS;
    }
}
