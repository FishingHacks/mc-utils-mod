package net.fishinghacks.utils.commands;

import net.fishinghacks.utils.calc.CalcContext;
import net.fishinghacks.utils.calc.MathException;
import net.fishinghacks.utils.calc.parsing.Parser;
import net.fishinghacks.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class QuickCalcCommand extends DotCommand {
    @Override
    public String getName() {
        return "=";
    }

    @Override
    public void run(String args, ChatListener listener) {
        args = args.trim();
        try {
            Parser parser = new Parser(args);
            var expr = parser.parseExpectEnd();
            var ctx = CalcContext.getDefault();
            var result = expr.eval(ctx);
            var resultString = result.toString();
            MutableComponent formattedResult = Translation.CmdCalcResult.with(
                Component.literal(args).withStyle(ChatFormatting.AQUA),
                Component.literal(resultString).withStyle(ChatFormatting.GREEN)).append(" ").append(
                Translation.ClickToCopy.get().copy().withStyle(ChatFormatting.DARK_AQUA)
                    .withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(resultString))));
            listener.handleSystemMessage(formattedResult, false);
        } catch (MathException e) {
            listener.handleSystemMessage(Component.literal(e.source.isEmpty() ? args : e.source), false);
            MutableComponent comp = Component.literal(" ".repeat(e.characterPos) + "^- ")
                .append(Translation.Error.get()).append(e.message).withStyle(ChatFormatting.RED);
            listener.handleSystemMessage(comp, false);
        }
    }
}
