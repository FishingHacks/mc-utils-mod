package net.fishinghacks.utils.client.connection;

import net.fishinghacks.utils.client.UtilsClient;
import net.fishinghacks.utils.client.cosmetics.CapeHandler;
import net.fishinghacks.utils.client.gui.GuiOverlayManager;
import net.fishinghacks.utils.client.gui.components.Notification;
import net.fishinghacks.utils.common.CommonUtil;
import net.fishinghacks.utils.common.Translation;
import net.fishinghacks.utils.common.Utils;
import net.fishinghacks.utils.common.connection.Connection;
import net.fishinghacks.utils.common.connection.packets.ClientPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;

import java.util.UUID;

import static net.fishinghacks.utils.client.gui.PauseMenuScreen.SAVING_LEVEL;

public class ClientPacketHandlerImpl implements ClientPacketHandler {
    private @Nullable String transformSlug(String slug, String invitingPlayer, boolean trusted) {
        if (trusted) return slug;
        if (CommonUtil.isInvalidFilename(slug)) {
            var inst = ClientConnectionHandler.getInstance();
            Utils.getLOGGER()
                .error("ERROR :: INVALID E4MC NAME AND REGION {} (from {}); Server: {} ({})", slug, invitingPlayer,
                    inst.getName(), inst.getIp());
            return null;
        }
        return slug + ".e4mc.link";
    }

    @Override
    public void handleInvite(String slug, String invitingPlayer, boolean trusted) {
        final String addr = transformSlug(slug, invitingPlayer, trusted);
        if (addr == null) return;
        if (!ServerAddress.isValidAddress(addr)) return;

        GuiOverlayManager.addNotification(
            (trusted ? Translation.Invite : Translation.InviteTrusted).with(invitingPlayer),
            new Notification.NotifyButton(Translation.InviteAccept.get(), ignored -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level != null) {
                    boolean flag = minecraft.isLocalServer();
                    minecraft.level.disconnect();
                    if (flag) {
                        minecraft.disconnect(new GenericMessageScreen(SAVING_LEVEL));
                    } else {
                        minecraft.disconnect();
                    }
                }
                Screen screen = UtilsClient.mainScreen();
                minecraft.setScreen(screen);
                if (!ServerAddress.isValidAddress(addr)) return;
                ServerData serverData = new ServerData(I18n.get("selectServer.defaultName"), addr,
                    ServerData.Type.OTHER);
                ConnectScreen.startConnecting(screen, minecraft, ServerAddress.parseString(addr), serverData, false,
                    null);
            }));
    }

    @Override
    public void handleInviteFailure() {
        GuiOverlayManager.addNotification(Translation.InviteFailed.get());
    }

    @Override
    public void handleGetNameResponse(String name) {
        name = name.trim();
        if (name.isEmpty()) return;
        ClientConnectionHandler.getInstance().setName(name);
    }

    @Override
    public void reloadCosmeticForPlayer(UUID player) {
        Minecraft.getInstance().schedule(() -> CapeHandler.removeProfile(player));
    }

    @Override
    public void onDisconnect(String reason, Connection connection) {
        GuiOverlayManager.addNotification(Translation.ServerDisconnected.get());
    }
}
