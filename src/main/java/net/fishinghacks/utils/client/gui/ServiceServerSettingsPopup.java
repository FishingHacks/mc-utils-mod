package net.fishinghacks.utils.client.gui;

import net.fishinghacks.utils.client.connection.ClientConnectionHandler;
import net.fishinghacks.utils.client.gui.components.Button;
import net.fishinghacks.utils.client.gui.components.Input;
import net.fishinghacks.utils.client.gui.components.Spacer;
import net.fishinghacks.utils.common.Colors;
import net.fishinghacks.utils.common.Translation;
import net.fishinghacks.utils.common.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.fishinghacks.utils.client.connection.ClientConnectionHandler.parseAddress;

public class ServiceServerSettingsPopup extends OverlayScreen {
    private Button connectButton;
    private Button disconnectButton;
    private Input ipInput;
    private static final Component connect = Translation.ServerConnect.get();
    private static final Component cancel = CommonComponents.GUI_CANCEL;
    private static final int inputLength = 200;
    private static final int historyButtonLength = inputLength + 2 * 4 + 2 * Button.SMALL_WIDTH;
    boolean repositionElements = false;

    public ServiceServerSettingsPopup(Screen parent) {
        super(Translation.ServerConnection.get(), parent, 400, 250);
    }

    @Override
    protected void init() {
        super.init();
        LinearLayout layout = new LinearLayout(getX() + 5, getY() + 18, LinearLayout.Orientation.VERTICAL).spacing(8);
        LinearLayout connectRow = layout.addChild(LinearLayout.horizontal()).spacing(4);
        ipInput = connectRow.addChild(Input.Builder.normal()
            .value(ipInput != null ? ipInput.getValue() : ClientConnectionHandler.getInstance().getIp())
            .width(inputLength).build());
        connectButton = connectRow.addChild(
            Button.Builder.small(Translation.ServerConnect.get()).onPress(this::connectButtonClick).build());
        disconnectButton = connectRow.addChild(Button.Builder.small(Translation.ServerDisconnect.get())
            .onPress(ignored -> ClientConnectionHandler.getInstance().disconnect("disconnect")).build());
        layout.addChild(new Spacer(0, 12));
        for (String s : Configs.clientConfig.SERVICE_SERVER_HISTORY.get().reversed()) {
            layout.addChild(Button.Builder.small(Component.literal(s)).width(historyButtonLength).onPress(ignored -> {
                ClientConnectionHandler.getInstance().abort();
                ClientConnectionHandler.getInstance().disconnect("reconnect");
                ipInput.setValue(s);
                ServerAddress address = parseAddress(s);
                if (address == null) return;
                ClientConnectionHandler.getInstance().connect(address);
            }).build());
        }

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int p_281550_, int p_282878_, float p_282465_) {
        if (repositionElements) {
            repositionElements = false;
            repositionElements();
        }
        connectButton.setMessage(ClientConnectionHandler.getInstance().isConnecting() ? cancel : connect);
        disconnectButton.active = ClientConnectionHandler.getInstance().isConnected();
        super.render(guiGraphics, p_281550_, p_282878_, p_282465_);

        Component message = ClientConnectionHandler.getInstance().getFormattedStatus();
        guiGraphics.drawString(font, message, ipInput.getX(), ipInput.getBottom() + 2, Colors.WHITE.get());
        Component title = Translation.ServerConnection.get();
        int titleX = getX() + ((overlayWidth - font.width(title)) / 2);
        guiGraphics.drawString(Minecraft.getInstance().font, title, titleX, getY() + 5, Colors.WHITE.get());
    }

    private void connectButtonClick(Button ignored) {
        if (ClientConnectionHandler.getInstance().isConnecting()) ClientConnectionHandler.getInstance().abort();
        else {
            ServerAddress address = parseAddress(ipInput.getValue());
            if (address == null) return;
            ClientConnectionHandler.getInstance().connect(address);
            var history = Configs.clientConfig.SERVICE_SERVER_HISTORY.get();
            List<String> newHistory = new ArrayList<>();
            for (var v : history) {
                if (!v.equals(ipInput.getValue())) newHistory.add(v);
            }
            if (newHistory.size() >= 8) newHistory.removeFirst();
            newHistory.add(ipInput.getValue());
            Configs.clientConfig.SERVICE_SERVER_HISTORY.set(newHistory);
            repositionElements = true;
        }
    }
}
