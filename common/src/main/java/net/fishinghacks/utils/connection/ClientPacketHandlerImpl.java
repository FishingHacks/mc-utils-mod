package net.fishinghacks.utils.connection;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.cosmetics.CosmeticHandler;
import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.fishinghacks.utils.gui.components.Notification;
import net.fishinghacks.utils.CommonUtil;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.connection.packets.ClientPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

import static net.fishinghacks.utils.gui.PauseMenuScreen.SAVING_LEVEL;

public class ClientPacketHandlerImpl implements ClientPacketHandler {
    private @Nullable String transformSlug(String slug, String invitingPlayer, boolean trusted) {
        if (trusted) return slug;
        if (CommonUtil.isInvalidFilename(slug)) {
            var inst = ClientConnectionHandler.getInstance();
            Constants.LOG.error("ERROR :: INVALID E4MC NAME AND REGION {} (from {}); Server: {} ({})", slug,
                invitingPlayer, inst.getName(), inst.getIp());
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
                Screen screen = Constants.mainScreen();
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
        Minecraft.getInstance().schedule(() -> CosmeticHandler.reloadCosmeticsForPlayer(player));
    }

    @Override
    public void onDisconnect(String reason, Connection connection) {
        GuiOverlayManager.addNotification(Translation.ServerDisconnected.get());
    }
}
