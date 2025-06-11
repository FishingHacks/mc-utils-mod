package net.fishinghacks.utils.gui;

import com.mojang.realmsclient.RealmsMainScreen;
import net.fishinghacks.utils.E4MCStore;
import net.fishinghacks.utils.Whitelist;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.components.Button;
import net.fishinghacks.utils.gui.components.IconButton;
import net.fishinghacks.utils.gui.components.IconTextButton;
import net.fishinghacks.utils.gui.configuration.ConfigSectionScreen;
import net.fishinghacks.utils.gui.cosmetics.CosmeticsScreen;
import net.fishinghacks.utils.gui.mcsettings.McSettingsScreen;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.connection.Connection;
import net.fishinghacks.utils.connection.packets.InvitePacket;
import net.fishinghacks.utils.gui.screenshots.ScreenshotsScreen;
import net.fishinghacks.utils.modules.ClickUi;
import net.fishinghacks.utils.platform.ClientServices;
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
    private static final Component GAME = Component.translatable("menu.game");
    private static final Component PAUSED = Component.translatable("menu.paused");
    public static final Component SAVING_LEVEL = Component.translatable("menu.savingLevel");
    public static final int BUTTONS_WIDTH = Button.SMALL_WIDTH * 2 + 4;

    private Button inviteButton;
    private Button cosmeticsButton;
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

        GridLayout gridLayout = new GridLayout((width - BUTTONS_WIDTH) / 2, height / 3);
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

        row.addChild(openScreen(OPTIONS, () -> new McSettingsScreen(this, minecraft.options)));
        if (this.minecraft.hasSingleplayerServer() && this.minecraft.getSingleplayerServer() != null && !this.minecraft.getSingleplayerServer()
            .isPublished()) row.addChild(openScreen(SHARE_TO_LAN, () -> new ShareToLanScreen(this)));
        else row.addChild(openScreen(PLAYER_REPORTING, () -> new SocialInteractionsScreen(this)));

        if (ClientServices.PLATFORM.hasModlistScreen()) row.addChild(new Button.Builder(Translation.Mods.get()).onPress(
                ignored -> ClientServices.PLATFORM.openModlistScreen(minecraft, this)).width((BUTTONS_WIDTH - 4) / 2)
            .build(), 2).setWidth(BUTTONS_WIDTH);

        Component component = this.minecraft.isLocalServer() ? RETURN_TO_MENU : CommonComponents.GUI_DISCONNECT;
        row.addChild(new Button.Builder(component).width(BUTTONS_WIDTH).onPress(button -> {
            button.active = false;
            minecraft.getReportingContext().draftReportHandled(minecraft, this, PauseMenuScreen::disconnect, true);
        }).build(), 2);
        gridLayout.arrangeElements();
        gridLayout.visitWidgets(this::addRenderableWidget);

        if (gridLayout.getX() + 300 + Button.DEFAULT_WIDTH + 5 < width) buildSidebarBig(gridLayout.getX());
        else buildSidebarSmall();
    }

    private void buildSidebarSmall() {
        assert minecraft != null;
        int y = height / 3;
        int x = width - 5 - Button.CUBE_WIDTH;

        inviteButton = addRenderableWidget(
            new IconButton.Builder(Icons.INVITE).onPress(PauseMenuScreen::invitePlayer).pos(x, y).build());
        y += inviteButton.getHeight() + 4;
        y += addRenderableWidget(new IconButton.Builder(Icons.MACROS).onPress(ignored -> MacrosScreen.open()).pos(x, y)
            .build()).getHeight() + 4;
        y += addRenderableWidget(new IconButton.Builder(Icons.SCREENSHOTS).onPress(
            ignored -> minecraft.setScreen(new ScreenshotsScreen(this))).pos(x, y).build()).getHeight() + 4;
        cosmeticsButton = addRenderableWidget(new IconButton.Builder(Icons.COSMETICS).pos(x, y)
            .onPress(ignored -> minecraft.setScreen(new CosmeticsScreen(this))).build());
        y += cosmeticsButton.getHeight() + 4;
        y += addRenderableWidget(new IconButton.Builder(Icons.SETTINGS).pos(x, y)
            .onPress(ignored -> minecraft.setScreen(new ConfigSectionScreen(this))).build()).getHeight() + 4;
        addRenderableWidget(
            new IconButton.Builder(Icons.MODULES).pos(x, y).onPress(ignored -> minecraft.setScreen(new ClickUi(this)))
                .build());
    }

    private void buildSidebarBig(int x) {
        assert minecraft != null;
        int y = height / 3;

        inviteButton = addRenderableWidget(
            new IconTextButton.Builder(Icons.INVITE, Translation.Invite.get()).x(x + 300).y(y)
                .onPress(PauseMenuScreen::invitePlayer).build());
        y += inviteButton.getHeight() + 4;
        y += addRenderableWidget(
            new IconTextButton.Builder(Icons.MACROS, Translation.MainGuiButtonMacros.get()).x(x + 300).y(y)
                .onPress(ignored -> MacrosScreen.open()).build()).getHeight() + 4;
        y += addRenderableWidget(
            new IconTextButton.Builder(Icons.SCREENSHOTS, Translation.ScreenshotGuiTitle.get()).onPress(
                ignored -> minecraft.setScreen(new ScreenshotsScreen(this))).x(x + 300).y(y).build()).getHeight() + 4;
        cosmeticsButton = addRenderableWidget(
            new IconTextButton.Builder(Icons.COSMETICS, Translation.CosmeticGuiTitle.get()).x(x + 300).y(y)
                .onPress(ignored -> minecraft.setScreen(new CosmeticsScreen(this))).build());
        y += cosmeticsButton.getHeight() + 4;
        y += addRenderableWidget(
            new IconTextButton.Builder(Icons.SETTINGS, Translation.MainGuiButtonSettings.get()).x(x + 300).y(y)
                .onPress(ignored -> minecraft.setScreen(new ConfigSectionScreen(this))).build()).getHeight() + 4;
        addRenderableWidget(
            new IconTextButton.Builder(Icons.MODULES, Translation.MainGuiButtonModules.get()).x(x + 300).y(y)
                .onPress(ignored -> minecraft.setScreen(new ClickUi(this))).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        inviteButton.active = inviteButton.visible = ClientConnectionHandler.getInstance()
            .isConnected() && E4MCStore.hasLink();
        cosmeticsButton.active = ClientConnectionHandler.getInstance().isConnected();

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
                sendInvite(conn, link, playerName);
            });
    }

    public static void sendInvite(Connection conn, String link, String playerName) {
        if (Whitelist.isWhitelisted(playerName)) conn.send(new InvitePacket(link, playerName));
        else ConfirmPopupScreen.open(Translation.InviteNotOnWhitelist.with(playerName), () -> {
            Whitelist.add(playerName);
            conn.send(new InvitePacket(link, playerName));
        });
    }
}
