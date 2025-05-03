package net.fishinghacks.utils.client.gui;

import com.mojang.realmsclient.RealmsMainScreen;
import net.fishinghacks.utils.client.E4MCStore;
import net.fishinghacks.utils.client.connection.ClientConnectionHandler;
import net.fishinghacks.utils.client.gui.components.Button;
import net.fishinghacks.utils.client.gui.components.IconButton;
import net.fishinghacks.utils.client.gui.mcsettings.McSettingsScreen;
import net.fishinghacks.utils.common.Translation;
import net.fishinghacks.utils.common.connection.Connection;
import net.fishinghacks.utils.common.connection.packets.InvitePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerLinksScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.CommonLinks;
import net.neoforged.neoforge.client.gui.ModListScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.function.Supplier;

public class PauseMenuScreen extends Screen {
    private static final Component RETURN_TO_GAME = Component.translatable("menu.returnToGame");
    private static final Component ADVANCEMENTS = Component.translatable("gui.advancements");
    private static final Component STATS = Component.translatable("gui.stats");
    private static final Component SEND_FEEDBACK = Component.translatable("menu.sendFeedback");
    private static final Component REPORT_BUGS = Component.translatable("menu.reportBugs");
    private static final Component FEEDBACK_SUBSCREEN = Component.translatable("menu.feedback");
    private static final Component SERVER_LINKS = Component.translatable("menu.server_links");
    private static final Component OPTIONS = Component.translatable("menu.options");
    private static final Component SHARE_TO_LAN = Component.translatable("menu.shareToLan");
    private static final Component PLAYER_REPORTING = Component.translatable("menu.playerReporting");
    private static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
    private static final Component MODS = Component.translatable("fml.menu.mods");
    private static final Component GAME = Component.translatable("menu.game");
    private static final Component PAUSED = Component.translatable("menu.paused");
    public static final Component SAVING_LEVEL = Component.translatable("menu.savingLevel");
    public static final int BUTTONS_HEIGHT = 6 * Button.DEFAULT_HEIGHT + 5 * 4 + 10;
    public static final int BUTTONS_WIDTH = Button.SMALL_WIDTH * 2 + 4;

    private Button inviteButton;
    private final boolean showPauseMenu;

    public PauseMenuScreen(boolean showPauseMenu) {
        super(showPauseMenu ? GAME : PAUSED);
        this.showPauseMenu = showPauseMenu;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(
            new StringWidget(0, this.showPauseMenu ? 40 : 10, this.width, 9, this.title, this.font));

        if (!showPauseMenu) return;

        GridLayout gridLayout = new GridLayout((width - BUTTONS_WIDTH) / 2, height / 2 - BUTTONS_HEIGHT);
        gridLayout.defaultCellSetting().padding(2);
        GridLayout.RowHelper row = gridLayout.createRowHelper(2);
        assert minecraft != null;
        assert minecraft.player != null;

        row.addChild(new Button.Builder(RETURN_TO_GAME).onPress(ignored -> {
            minecraft.setScreen(null);
            minecraft.mouseHandler.grabMouse();
        }).width(BUTTONS_WIDTH).build(), 2);

        row.addChild(openScreen(ADVANCEMENTS,
            () -> new AdvancementsScreen(minecraft.player.connection.getAdvancements(), this)));
        row.addChild(openScreen(STATS, () -> new StatsScreen(this, minecraft.player.getStats())));

        ServerLinks serverlinks = this.minecraft.player.connection.serverLinks();
        if (serverlinks.isEmpty() && SharedConstants.getCurrentVersion().getDataVersion().isSideSeries()) {
            row.addChild(openLink(SEND_FEEDBACK, SharedConstants.getCurrentVersion()
                .isStable() ? CommonLinks.RELEASE_FEEDBACK : CommonLinks.SNAPSHOT_FEEDBACK));
            row.addChild(openLink(REPORT_BUGS, CommonLinks.SNAPSHOT_BUGS_FEEDBACK));
        } else if (serverlinks.isEmpty()) {
            row.addChild(openLink(SEND_FEEDBACK, SharedConstants.getCurrentVersion()
                .isStable() ? CommonLinks.RELEASE_FEEDBACK : CommonLinks.SNAPSHOT_FEEDBACK), 2).setWidth(BUTTONS_WIDTH);
        } else {
            row.addChild(openScreen(FEEDBACK_SUBSCREEN, () -> this));
            row.addChild(openScreen(SERVER_LINKS, () -> new ServerLinksScreen(this, serverlinks)));
        }

        Button lanButton;
        row.addChild(openScreen(OPTIONS, () -> new McSettingsScreen(this, minecraft.options)));
        if (this.minecraft.hasSingleplayerServer() && this.minecraft.getSingleplayerServer() != null && !this.minecraft.getSingleplayerServer()
            .isPublished()) lanButton = row.addChild(openScreen(SHARE_TO_LAN, () -> new ShareToLanScreen(this)));
        else lanButton = row.addChild(openScreen(PLAYER_REPORTING, () -> new SocialInteractionsScreen(this)));

        row.addChild(openScreen(MODS, () -> new ModListScreen(this)), 2).setWidth(BUTTONS_WIDTH);

        Component component = this.minecraft.isLocalServer() ? RETURN_TO_MENU : CommonComponents.GUI_DISCONNECT;
        row.addChild(new Button.Builder(component).width(BUTTONS_WIDTH).onPress(button -> {
            button.active = false;
            minecraft.getReportingContext().draftReportHandled(minecraft, this, PauseMenuScreen::disconnect, true);
        }).build(), 2);
        gridLayout.arrangeElements();
        gridLayout.visitWidgets(this::addRenderableWidget);

        inviteButton = addRenderableWidget(
            new IconButton.Builder(Icons.INVITE).pos(lanButton.getRight() + 4, lanButton.getY())
                .onPress(PauseMenuScreen::invitePlayer).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        inviteButton.active = inviteButton.visible = ClientConnectionHandler.getInstance()
            .isConnected() && E4MCStore.hasLink();

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private Button openScreen(Component message, Supplier<Screen> screenSupplier) {
        assert minecraft != null;
        return new Button.Builder(message).onPress(ignored -> minecraft.setScreen(screenSupplier.get()))
            .width((BUTTONS_WIDTH - 4) / 2).build();
    }

    private Button openLink(Component message, URI link) {
        assert minecraft != null;
        return new Button.Builder(message).onPress(
            ignored -> ConfirmLinkScreen.confirmLinkNow(PauseMenuScreen.this, link)).build();
    }

    public static void disconnect() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        boolean flag = minecraft.isLocalServer();
        ServerData serverdata = minecraft.getCurrentServer();
        minecraft.level.disconnect();
        if (flag) {
            minecraft.disconnect(new GenericMessageScreen(SAVING_LEVEL));
        } else {
            minecraft.disconnect();
        }

        TitleScreen titlescreen = new TitleScreen();
        if (flag) {
            minecraft.setScreen(titlescreen);
        } else if (serverdata != null && serverdata.isRealm()) {
            minecraft.setScreen(new RealmsMainScreen(titlescreen));
        } else {
            minecraft.setScreen(new JoinMultiplayerScreen(titlescreen));
        }
    }

    public static <T> void invitePlayer(@Nullable T ignored) {
        InputPopupScreen.open(Translation.InviteGuiTitle.get(),
            Translation.InvitePlayerName.get().copy().withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
            playerName -> {
                String link = E4MCStore.getLink();
                if (link == null) return;
                Connection conn = ClientConnectionHandler.getInstance().getConnection();
                if (conn == null) return;
                conn.send(new InvitePacket(link, playerName));
            });
    }
}
