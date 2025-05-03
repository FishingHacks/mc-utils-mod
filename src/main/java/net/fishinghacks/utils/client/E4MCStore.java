package net.fishinghacks.utils.client;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fishinghacks.utils.common.Utils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.CommandEvent;
import org.slf4j.Logger;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = UtilsClient.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class E4MCStore {
    @Nullable
    private static String link;

    public static void domainAssigned(String domain) {
        if(!domain.contains(".e4mc.link")) return;
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

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        remove();
    }
    @SubscribeEvent
    public static void onConnect(ClientPlayerNetworkEvent.LoggingIn event) {
        remove();
    }

    @SubscribeEvent
    public static void onCommandExecuted(CommandEvent event) {
        Logger logger = Utils.getLOGGER();
        var nodes = event.getParseResults().getContext().getNodes();
        if(nodes.size() != 2) return;
        if(!(nodes.getFirst().getNode() instanceof LiteralCommandNode<?> litCmd)) return;
        if(!litCmd.getLiteral().equals("e4mc")) return;
        remove();
    }
}
