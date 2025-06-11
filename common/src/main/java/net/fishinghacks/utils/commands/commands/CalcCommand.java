package net.fishinghacks.utils.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.commands.Command;
import net.fishinghacks.utils.commands.arguments.RunningCalculationArgument;
import net.fishinghacks.utils.macros.Executor;
import net.fishinghacks.utils.macros.RunningMacro;
import net.fishinghacks.utils.macros.exprs.LiteralValue;
import net.fishinghacks.utils.mixin.client.IngredientAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
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
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.Consumer;

public class CalcCommand extends Command {
    private static final ContextMap EMPTY = new ContextMap.Builder().create(new ContextKeySet.Builder().build());

    private static final HashMap<Integer, RunningMacro> calculations = new HashMap<>();
    private static int currentIndex = 0;

    public CalcCommand() {
        super("calc");
    }

    public static Set<Integer> runningCalculations() {
        return calculations.keySet();
    }

    public static void runCalculation(String content, ChatListener listener) {
        int index = currentIndex++;
        var calcStopFormatted = Component.literal(".calc ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal("stop ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(String.valueOf(index)).withStyle(ChatFormatting.GREEN));
        var stopAction = Translation.ClickHere.with().withStyle(ChatFormatting.DARK_AQUA)
            .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("stop_calc " + index)));
        listener.handleSystemMessage(Translation.CmdCalcRunning.with(index, calcStopFormatted, stopAction), false);
        var macro = Executor.runInThread(content, "<input>", new HashMap<>(),
            v -> listener.handleSystemMessage(v, false), v -> formatOutput(v, content, index, listener),
            (ignored) -> Optional.empty(), () -> calculations.remove(index));
        calculations.put(index, macro);
    }

    public static void calculate(String content, ChatListener listener, Consumer<@Nullable LiteralValue> onFinish) {
        int index = currentIndex++;
        var calcStopFormatted = Component.literal(".calc ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal("stop ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(String.valueOf(index)).withStyle(ChatFormatting.GREEN))
            .withStyle(style -> style.withClickEvent(new ClickEvent.SuggestCommand(".calc stop " + index)));
        var stopAction = Translation.ClickHere.with().withStyle(ChatFormatting.DARK_AQUA)
            .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("stop_calc " + index)));
        listener.handleSystemMessage(Translation.CmdCalcRunning.with(index, calcStopFormatted, stopAction), false);

        boolean[] didFinish = new boolean[]{false};
        var macro = Executor.runInThread(content, "<input>", new HashMap<>(), v -> {
            listener.handleSystemMessage(v, false);
            if (!didFinish[0]) {
                onFinish.accept(null);
                didFinish[0] = true;
            }
        }, onFinish, (ignored) -> Optional.empty(), () -> calculations.remove(index));
        calculations.put(index, macro);
    }

    private static void formatOutput(LiteralValue output, String content, int index, ChatListener listener) {
        var formatted = output.formatted();
        MutableComponent comp;
        if (content.length() < 20) comp = Translation.CmdCalcResult.with(content);
        else comp = Translation.CmdCalcResultResultLong.with(index);
        if (!formatted.isEmpty()) comp.append(formatted.getFirst());
        listener.handleSystemMessage(comp, false);
        for (int i = 1; i < formatted.size(); ++i) listener.handleSystemMessage(formatted.get(i), false);
    }

    private void craftOutput(String item, int amount, ChatListener listener) {
        ClientPacketListener packetListener = Minecraft.getInstance().getConnection();
        if (packetListener == null) return;
        var res = packetListener.searchTrees().recipes().search(item.toLowerCase(Locale.ROOT));
        if (res.isEmpty() && !item.contains(":"))
            res = packetListener.searchTrees().recipes().search("minecraft:" + item.toLowerCase(Locale.ROOT));
        if (res.isEmpty()) {
            listener.handleSystemMessage(Translation.CmdCalcCraftNotFound.with(item).withStyle(ChatFormatting.RED),
                false);
            return;
        }
        var recipes = res.getFirst();
        if (recipes.getRecipes().isEmpty()) {
            listener.handleSystemMessage(Translation.CmdCalcCraftNotFound.with(item).withStyle(ChatFormatting.RED),
                false);
            return;
        }
        var recipe = recipes.getRecipes().getFirst();
        var requirements = recipe.craftingRequirements();
        assert requirements.isPresent();
        calculateCraft(recipe.resultItems(EMPTY).getFirst(), requirements.orElse(List.of()), amount, listener);
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
            final var values = ((IngredientAccessor) (Object) ingredient).getValues();
            if (values.size() == 1) items.compute(values.get(0).value(), (i1, count) -> count == null ? 1 : count + 1);
            else values.unwrap().map(key -> tags.compute(key,
                    (i1, count) -> count == null ? new Pair<>(1, values) : new Pair<>(count.getA() + 1, count.getB())),
                extras::add);
        }
        listener.handleSystemMessage(
            Translation.CmdCalcCraftResult.with(result.getItemName().copy().withStyle(ChatFormatting.AQUA),
                number(amount)), false);

        //todo: figure out what's broken here? items, tags and extras are clearly changed on the lines above (for loop)
        //noinspection RedundantOperationOnEmptyContainer
        items.forEach((item, count) -> listener.handleSystemMessage(
            craftEntry(item.getName().copy().withStyle(ChatFormatting.AQUA), count * numCrafts), false));

        //noinspection RedundantOperationOnEmptyContainer
        tags.forEach((tag, count) -> listener.handleSystemMessage(craftEntry(
                addList(count.getB().stream().map(Holder::value).toList(),
                    Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_AQUA)),
                numCrafts * count.getA()),
            false));

        //noinspection RedundantOperationOnEmptyContainer
        extras.forEach(v -> listener.handleSystemMessage(craftEntry(addList(v.stream().map(Holder::value).toList(),
            Translation.CmdCalcCraftMultiple.with().withStyle(ChatFormatting.DARK_AQUA)), numCrafts), false));
    }

    private MutableComponent addList(List<Item> items, MutableComponent component) {
        MutableComponent comp = Translation.CmdCalcCraftOneOf.with();
        for (var item : items) comp.append("\n").append(item.getName());
        return component.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(comp)));
    }

    private MutableComponent craftEntry(Component item, int amount) {
        return Translation.CmdCalcCraftEntry.with(item, number(amount), number((double) amount / 64),
            // 3 * 9 * 64
            number((double) amount / 1728));
    }

    private int calc(CommandContext<SharedSuggestionProvider> context) {
        runCalculation(context.getArgument("expression", String.class), Minecraft.getInstance().getChatListener());
        return SINGLE_SUCCESS;
    }

    private int nether_overworld(boolean overworld, CommandContext<SharedSuggestionProvider> context) {
        int x = context.getArgument("x", Integer.class);
        int y = context.getArgument("y", Integer.class);
        int z = context.getArgument("z", Integer.class);

        int newX = overworld ? x * 8 : x / 8;
        int newZ = overworld ? z * 8 : z / 8;
        MutableComponent comp = copyable("X: " + x + " Y: " + y + " Z: " + z).append(
            Component.literal(" >> ").withStyle(ChatFormatting.DARK_GRAY)).append(
            (overworld ? Translation.CmdCalcOverworld : Translation.CmdCalcNether).get().copy()
                .withStyle(ChatFormatting.AQUA)).append(" ").append("X: " + newX + " Y: " + y + " Z: " + newZ);
        Minecraft.getInstance().getChatListener().handleSystemMessage(comp, false);

        return SINGLE_SUCCESS;
    }

    private int nether_overworld_player_pos(boolean overworld) {
        if (Minecraft.getInstance().player == null) return 0;
        int x = Minecraft.getInstance().player.getBlockX();
        int y = Minecraft.getInstance().player.getBlockY();
        int z = Minecraft.getInstance().player.getBlockZ();

        int newX = overworld ? x * 8 : x / 8;
        int newZ = overworld ? z * 8 : z / 8;
        MutableComponent comp = copyable("X: " + x + " Y: " + y + " Z: " + z).append(
            Component.literal(" >> ").withStyle(ChatFormatting.DARK_GRAY)).append(
            (overworld ? Translation.CmdCalcOverworld : Translation.CmdCalcNether).get().copy()
                .withStyle(ChatFormatting.AQUA)).append(" ").append("X: " + newX + " Y: " + y + " Z: " + newZ);
        Minecraft.getInstance().getChatListener().handleSystemMessage(comp, false);

        return SINGLE_SUCCESS;
    }

    private int stop(CommandContext<SharedSuggestionProvider> context) {
        calculations.remove(context.getArgument("calc_id", Integer.class));
        return SINGLE_SUCCESS;
    }

    private int craft_no_amount(CommandContext<SharedSuggestionProvider> context) {
        var maybe_item = ItemArgument.getItem(context, "item").getItem().getDefaultInstance().getItemHolder()
            .unwrapKey();
        if (maybe_item.isEmpty()) return -1;
        craftOutput(maybe_item.get().location().toString(), 1, Minecraft.getInstance().getChatListener());
        return SINGLE_SUCCESS;
    }

    private int craft_amount(CommandContext<SharedSuggestionProvider> context) {
        var maybe_item = ItemArgument.getItem(context, "item").getItem().getDefaultInstance().getItemHolder()
            .unwrapKey();
        if (maybe_item.isEmpty()) return -1;
        String itemName = maybe_item.get().location().toString();
        calculate(context.getArgument("amount", String.class), Minecraft.getInstance().getChatListener(), result -> {
            if (result != null)
                craftOutput(itemName, (int) Math.floor(result.asDouble()), Minecraft.getInstance().getChatListener());
        });
        return SINGLE_SUCCESS;
    }

    @Override
    protected void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder, CommandBuildContext context) {
        builder.then(
                literal("calc").then(argument("expression", StringArgumentType.greedyString()).executes(this::calc))).then(
                literal("nether").executes(ignored -> nether_overworld_player_pos(false)).then(
                    argument("x", IntegerArgumentType.integer()).then(argument("y", IntegerArgumentType.integer()).then(
                        argument("z", IntegerArgumentType.integer()).executes(ctx -> nether_overworld(false, ctx)))))).then(
                literal("overworld").executes(ignored -> nether_overworld_player_pos(true)).then(
                    argument("x", IntegerArgumentType.integer()).then(argument("y", IntegerArgumentType.integer()).then(
                        argument("z", IntegerArgumentType.integer()).executes(ctx -> nether_overworld(true, ctx))))))
            .then(literal("stop").then(argument("calc_id", RunningCalculationArgument.INSTANCE).executes(this::stop)))
            .then(literal("craft").then(argument("item", ItemArgument.item(context)).executes(this::craft_no_amount)
                .then(argument("amount", StringArgumentType.greedyString()).executes(this::craft_amount))));
    }
}
