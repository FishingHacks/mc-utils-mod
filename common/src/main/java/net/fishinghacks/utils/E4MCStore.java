package net.fishinghacks.utils;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;

import org.jetbrains.annotations.Nullable;

public class E4MCStore {
    @Nullable
    private static String link;

    public static void domainAssigned(String domain) {
        if (!domain.contains(".e4mc.link")) return;
        link = domain.substring(0, domain.length() - 10).strip();
    }

    public static void remove() {
        link = null;
    }

    @Nullable
    public static String getLink() {
        return link;
    }

    public static boolean hasLink() {
        return link != null;
    }

    public static void onDisconnect() {
        remove();
    }

    public static void onConnect() {
        remove();
    }


    public static void onCommandExecuted(ParseResults<CommandSourceStack> parseResults) {
        var nodes = parseResults.getContext().getNodes();
        if (nodes.size() != 2) return;
        if (!(nodes.getFirst().getNode() instanceof LiteralCommandNode<?> litCmd)) return;
        if (!litCmd.getLiteral().equals("e4mc")) return;
        remove();
    }
}
