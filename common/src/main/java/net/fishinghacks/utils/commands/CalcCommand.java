package net.fishinghacks.utils.commands;

import net.fishinghacks.utils.calc.CalcContext;
import net.fishinghacks.utils.calc.CustomFunction;
import net.fishinghacks.utils.calc.MathException;
import net.fishinghacks.utils.calc.exprs.Expression;
import net.fishinghacks.utils.calc.exprs.LiteralValue;
import net.fishinghacks.utils.calc.parsing.Parser;
import net.fishinghacks.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.*;

public class CalcCommand extends ArgDotCommand {
    private static final ContextMap EMPTY = new ContextMap.Builder().create(new ContextKeySet.Builder().build());

    @Override
    public String getName() {
        return "calc";
    }

    private @Nullable LiteralValue evaluate(String content, ChatListener listener) {
        try {
            Parser parser = new Parser(content);
            var expr = parser.parseExpectEnd();
            var ctx = CalcContext.getDefault();
            return expr.eval(ctx);
        } catch (MathException e) {
            listener.handleSystemMessage(Component.literal(e.source.isEmpty() ? content : e.source), false);
            MutableComponent comp = Component.literal(" ".repeat(e.characterPos) + "^- ")
                .append(Translation.Error.get()).append(e.message).withStyle(ChatFormatting.RED);
            listener.handleSystemMessage(comp, false);
            return null;
        }
    }

    private String join(String[] content, int startPos) {
        StringBuilder contentString = new StringBuilder();
        for (int i = startPos; i < content.length; ++i) contentString.append(content[i]);
        return contentString.toString();
    }

    @Override
    public void run(String[] args, ChatListener listener) {
        switch (args[0]) {
            case "calc" -> {
                String content = join(args, 1);
                var result = evaluate(content, listener);
                if (result == null) return;
                var stringifiedResult = result.toString();
                MutableComponent formattedResult = Translation.CmdCalcResult.with(
                    Component.literal(content).withStyle(ChatFormatting.AQUA),
                    Component.literal(stringifiedResult).withStyle(ChatFormatting.GREEN)).append(" ").append(
                    Translation.ClickToCopy.get().copy().withStyle(ChatFormatting.DARK_AQUA)
                        .withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(stringifiedResult))));
                listener.handleSystemMessage(formattedResult, false);
            }
            case "nether", "overworld" -> {
                int x;
                int y;
                int z;
                if (args.length == 1) {
                    var player = Minecraft.getInstance().player;
                    if (player == null) return;
                    x = player.getBlockX();
                    y = player.getBlockY();
                    z = player.getBlockZ();
                } else if (args.length == 4) {
                    try {
                        x = Integer.parseInt(args[1]);
                        y = Integer.parseInt(args[2]);
                        z = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        listener.handleSystemMessage(
                            Translation.CmdCalcParseIntFailed.get().copy().withStyle(ChatFormatting.RED), false);
                        return;
                    }
                } else {
                    listener.handleSystemMessage(
                        Translation.CmdCalcNetherOverworldUsage.with(args[0]).withStyle(ChatFormatting.RED), false);
                    return;
                }
                int newX = args[0].equals("nether") ? x / 8 : x * 8;
                int newZ = args[0].equals("nether") ? z / 8 : z * 8;
                MutableComponent comp = copyable("X: " + x + " Y: " + y + " Z: " + z).append(
                    Component.literal(" >> ").withStyle(ChatFormatting.DARK_GRAY)).append(
                    (args[0].equals("nether") ? Translation.CmdCalcNether : Translation.CmdCalcOverworld).get().copy()
                        .withStyle(ChatFormatting.AQUA)).append(" ").append("X: " + newX + " Y: " + y + " Z: " + newZ);
                listener.handleSystemMessage(comp, false);
            }
            case "custom" -> {
                switch (args[1]) {
                    case "add" -> {
                        if (args.length < 5) {
                            listener.handleSystemMessage(
                                Translation.CmdCalcCustomAddUsage.get().copy().withStyle(ChatFormatting.RED), false);
                            return;
                        }
                        int equalidx = 0;
                        for (int i = 3; i < args.length - 1; ++i) {
                            if (args[i].equals("=")) {
                                equalidx = i;
                                break;
                            }
                        }
                        if (equalidx == 0) {
                            listener.handleSystemMessage(
                                Translation.CmdCalcCustomAddUsage.get().copy().withStyle(ChatFormatting.RED), false);
                            return;
                        }
                        String name = args[2];
                        var variables = Arrays.stream(args).skip(3).limit(equalidx - 3).toList();
                        Expression expr;
                        String content = join(args, equalidx + 1);
                        try {
                            Parser parser = new Parser(content);
                            expr = parser.parseExpectEnd();
                        } catch (MathException e) {
                            listener.handleSystemMessage(Component.literal(e.source.isEmpty() ? content : e.source),
                                false);
                            MutableComponent comp = Component.literal(" ".repeat(e.characterPos) + "^- ")
                                .append(Translation.Error.get()).append(e.message).withStyle(ChatFormatting.RED);
                            listener.handleSystemMessage(comp, false);
                            return;
                        }
                        CalcContext.customFunctions.put(name, new CustomFunction(content, expr, variables));
                        MutableComponent comp = Translation.CmdCalcCustomAdded.get().copy()
                            .append(Component.literal(" " + name).withStyle(ChatFormatting.AQUA));
                        for (var arg : variables) comp = comp.append(" " + arg);
                        comp.append(" = ").append(content);
                        listener.handleSystemMessage(comp, false);
                    }
                    case "remove" -> {
                        CalcContext.customFunctions.remove(args[2]);
                        listener.handleSystemMessage(Translation.CmdCalcParseCustomFuncRemoved.with(
                            Component.literal(args[2]).withStyle(ChatFormatting.AQUA)), false);
                    }
                    case "list" -> CalcContext.customFunctions.forEach((k, v) -> {
                        MutableComponent comp = Component.empty()
                            .append(Component.literal(k).withStyle(ChatFormatting.AQUA));
                        for (var arg : v.variables()) comp.append(" " + arg);
                        comp.append(" = ");
                        comp.append(v.source());
                        listener.handleSystemMessage(comp, false);
                    });
                    default -> listener.handleSystemMessage(
                        Translation.CmdCalcInvalidSubcommand.with("custom " + args[1]).withStyle(ChatFormatting.RED),
                        false);
                }
            }
            case "craft" -> {
                if (args.length < 2) {
                    listener.handleSystemMessage(
                        Translation.CmdCalcCraftUsage.get().copy().withStyle(ChatFormatting.RED), false);
                    return;
                }
                int amount = 1;
                if (args.length > 2) {
                    var result = evaluate(join(args, 2), listener);
                    if (result == null) return;
                    amount = (int) Math.floor(result.value());
                }

                ClientPacketListener packetListener = Minecraft.getInstance().getConnection();
                if (packetListener == null) return;
                var res = packetListener.searchTrees().recipes().search(args[1].toLowerCase(Locale.ROOT));
                if (res.isEmpty() && !args[1].contains(":")) res = packetListener.searchTrees().recipes()
                    .search("minecraft:" + args[1].toLowerCase(Locale.ROOT));
                if (res.isEmpty()) {
                    listener.handleSystemMessage(
                        Translation.CmdCalcCraftNotFound.with(args[1]).withStyle(ChatFormatting.RED), false);
                    return;
                }
                var recipes = res.getFirst();
                if (recipes.getRecipes().isEmpty()) {
                    listener.handleSystemMessage(
                        Translation.CmdCalcCraftNotFound.with(args[1]).withStyle(ChatFormatting.RED), false);
                    return;
                }
                var recipe = recipes.getRecipes().getFirst();
                var requirements = recipe.craftingRequirements();
                assert requirements.isPresent();
                calculateCraft(recipe.resultItems(EMPTY).getFirst(), requirements.orElse(List.of()), amount,
                    listener);
            }
            default -> listener.handleSystemMessage(
                Translation.CmdCalcInvalidSubcommand.with(args[0]).withStyle(ChatFormatting.RED), false);
        }
    }

    private MutableComponent copyable(String value) {
        return Component.literal(value).withStyle(
            style -> style.withClickEvent(new ClickEvent.CopyToClipboard(value)).withColor(ChatFormatting.GREEN));
    }

    private MutableComponent number(double value) {
        return copyable(LiteralValue.doubleToString(value));
    }

    private void calculateCraft(ItemStack result, List<Ingredient> requirements, int amount, ChatListener listener) {
        int numCrafts = Math.ceilDiv(amount, result.getCount());
        amount = result.getCount() * numCrafts;

        Map<TagKey<Item>, Pair<Integer, HolderSet<Item>>> tags = new HashMap<>();
        Map<Item, Integer> items = new HashMap<>();
        List<List<Holder<Item>>> extras = new ArrayList<>();

        for (Ingredient ingredient : requirements) {

            if (ingredient.values.size() == 1)
                items.compute(ingredient.values.get(0).value(), (i1, count) -> count == null ? 1 : count + 1);
            else ingredient.values.unwrap().map(key -> tags.compute(key,
                (i1, count) -> count == null ? new Pair<>(1, ingredient.values) : new Pair<>(count.getA() + 1,
                    count.getB())), extras::add);
        }
        listener.handleSystemMessage(
            Translation.CmdCalcCraftResult.with(result.getItemName().copy().withStyle(ChatFormatting.AQUA),
                number(amount)), false);

        items.forEach((item, count) -> listener.handleSystemMessage(
            craftEntry(item.getName().copy().withStyle(ChatFormatting.AQUA), count * numCrafts), false));

        tags.forEach((tag, count) -> listener.handleSystemMessage(craftEntry(
                addList(count.getB().stream().map(Holder::value).toList(),
                    Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_AQUA)),
                numCrafts * count.getA()),
            false));

        extras.forEach(v -> listener.handleSystemMessage(craftEntry(addList(v.stream().map(Holder::value).toList(),
            Translation.CmdCalcCraftMultiple.with().withStyle(ChatFormatting.DARK_AQUA)), numCrafts), false));
    }

    private MutableComponent addList(List<Item> items, MutableComponent component) {
        MutableComponent comp = Translation.CmdCalcCraftOneOf.with();
        for (var item : items) comp.append("\n").append(item.getName());
        return component.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(comp)));
    }

    private MutableComponent craftEntry(Component item, int amount) {
        return Translation.CmdCalcCraftEntry.with(item, number(amount),
            number((double) amount / CalcContext.ITEMS_PER_STACK),
            number((double) amount / CalcContext.ITEMS_PER_SHULKERBOX));
    }
}
