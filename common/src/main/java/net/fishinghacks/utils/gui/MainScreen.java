package net.fishinghacks.utils.gui;

import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.components.Button;
import net.fishinghacks.utils.gui.components.IconButton;
import net.fishinghacks.utils.gui.components.IconTextButton;
import net.fishinghacks.utils.gui.configuration.ConfigSectionScreen;
import net.fishinghacks.utils.gui.cosmetics.CosmeticsScreen;
import net.fishinghacks.utils.gui.mcsettings.McSettingsScreen;
import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.gui.screenshots.ScreenshotsScreen;
import net.fishinghacks.utils.modules.ClickUi;
import net.fishinghacks.utils.platform.ClientServices;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class MainScreen extends Screen {
    private static final Component TITLE = Component.translatable("narrator.screen.title");
    private static final Component COPYRIGHT_TEXT = Component.translatable("title.credits");
    public static final ResourceLocation MINECRAFT_LOGO = ResourceLocation.withDefaultNamespace(
        "textures/gui/title/minecraft.png");
    public static final int LOGO_WIDTH = 256;
    public static final int LOGO_HEIGHT = 44;
    public static final int LOGO_TEXTURE_WIDTH = 256;
    public static final int LOGO_TEXTURE_HEIGHT = 64;
    public static final Component SINGLEPLAYER = Component.translatable("menu.singleplayer");
    public static final Component MULTIPLAYER = Component.translatable("menu.multiplayer");
    public static final Component OPTIONS = Component.translatable("menu.options");
    public static final Component QUIT = Component.translatable("menu.quit");
    @Nullable
    private SplashRenderer splash;
    @Nullable
    private Button cosmeticButton;

    public MainScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        assert this.minecraft != null;
        if (splash == null) this.splash = this.minecraft.getSplashManager().getSplash();

        int copyrightTextWidth = this.font.width(COPYRIGHT_TEXT);
        int copyrightTextX = this.width - copyrightTextWidth - 2;
        this.addRenderableWidget(new PlainTextButton(copyrightTextX, height - 10, copyrightTextX, 10, COPYRIGHT_TEXT,
            ignored -> minecraft.setScreen(new CreditsAndAttributionScreen(this)), font));

        int y = height / 3;
        int x = this.width / 2 - 90;

        y += this.addRenderableWidget(
            new Button.Builder(SINGLEPLAYER).onPress(btn -> this.minecraft.setScreen(new SelectWorldScreen(this)))
                .pos(x, y).width(180).build()).getHeight() + 4;

        y += this.addRenderableWidget(new Button.Builder(MULTIPLAYER).onPress(btn -> {
            Screen screen = this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(
                this) : new SafetyScreen(this);
            minecraft.setScreen(screen);
        }).pos(x, y).width(180).build()).getHeight() + 4;

        if (ClientServices.PLATFORM.hasModlistScreen()) y += this.addRenderableWidget(
                new Button.Builder(Translation.Mods.get()).onPress(
                    btn -> ClientServices.PLATFORM.openModlistScreen(this.minecraft, this)).pos(x, y).width(180).build())
            .getHeight() + 8;

        this.addRenderableWidget(new Button.Builder(OPTIONS).onPress(
                btn -> this.minecraft.setScreen(new McSettingsScreen(this, this.minecraft.options))).pos(x, y).width(88)
            .build());
        this.addRenderableWidget(
            new Button.Builder(QUIT).onPress(btn -> this.minecraft.stop()).pos(width / 2 + 2, y).width(88).build());
        this.addRenderableWidget(new IconButton.Builder(Icons.LANGUAGE).onPress(btn -> this.minecraft.setScreen(
                new McSettingsScreen(this, minecraft.options, McSettingsScreen.OptionType.Language))).y(y)
            .x(x - 4 - IconButton.DEFAULT_WIDTH).build());
        this.addRenderableWidget(new IconButton.Builder(Icons.ACCESSIBILITY).onPress(btn -> this.minecraft.setScreen(
                new McSettingsScreen(this, this.minecraft.options, McSettingsScreen.OptionType.Accessibility))).y(y)
            .x(x + 184).build());

        if (x + 300 + Button.DEFAULT_WIDTH + 5 < width) buildSidebarBig(x);
        else buildSidebarSmall();
    }

    private void buildSidebarSmall() {
        assert minecraft != null;
        int y = height / 3;
        int x = width - 5 - Button.CUBE_WIDTH;

        y += addRenderableWidget(new IconButton.Builder(Icons.MACROS).pos(x, y).onPress(ignored -> MacrosScreen.open())
            .build()).getHeight() + 4;
        y += addRenderableWidget(new IconButton.Builder(Icons.SCREENSHOTS).onPress(
            ignored -> minecraft.setScreen(new ScreenshotsScreen(this))).pos(x, y).build()).getHeight() + 4;
        cosmeticButton = addRenderableWidget(new IconButton.Builder(Icons.COSMETICS).pos(x, y)
            .onPress(ignored -> minecraft.setScreen(new CosmeticsScreen(this))).build());
        y += cosmeticButton.getHeight() + 4;
        y += addRenderableWidget(new IconButton.Builder(Icons.SETTINGS).pos(x, y)
            .onPress(ignored -> minecraft.setScreen(new ConfigSectionScreen(this))).build()).getHeight() + 4;
        addRenderableWidget(
            new IconButton.Builder(Icons.MODULES).pos(x, y).onPress(ignored -> minecraft.setScreen(new ClickUi(this)))
                .build());
    }

    private void buildSidebarBig(int x) {
        assert minecraft != null;
        int y = height / 3;
        y += addRenderableWidget(
            new IconTextButton.Builder(Icons.MACROS, Translation.MainGuiButtonMacros.get()).x(x + 300).y(y)
                .onPress(ignored -> MacrosScreen.open()).build()).getHeight() + 4;
        y += addRenderableWidget(
            new IconTextButton.Builder(Icons.SCREENSHOTS, Translation.ScreenshotGuiTitle.get()).onPress(
                ignored -> minecraft.setScreen(new ScreenshotsScreen(this))).x(x + 300).y(y).build()).getHeight() + 4;
        cosmeticButton = addRenderableWidget(
            new IconTextButton.Builder(Icons.COSMETICS, Translation.CosmeticGuiTitle.get()).x(x + 300).y(y)
                .onPress(ignored -> minecraft.setScreen(new CosmeticsScreen(this))).build());
        y += cosmeticButton.getHeight() + 4;
        y += addRenderableWidget(
            new IconTextButton.Builder(Icons.SETTINGS, Translation.MainGuiButtonSettings.get()).x(x + 300).y(y)
                .onPress(ignored -> minecraft.setScreen(new ConfigSectionScreen(this))).build()).getHeight() + 4;
        addRenderableWidget(
            new IconTextButton.Builder(Icons.MODULES, Translation.MainGuiButtonModules.get()).x(x + 300).y(y)
                .onPress(ignored -> minecraft.setScreen(new ClickUi(this))).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        assert minecraft != null;
        if (cosmeticButton != null) cosmeticButton.active = ClientConnectionHandler.getInstance().isConnected();
        this.renderPanorama(guiGraphics, partialTick);
        renderLogo(guiGraphics, width);
        if (splash != null && !minecraft.options.hideSplashTexts().get() && Configs.clientConfig.SHOW_SPLASH.get())
            splash.render(guiGraphics, width, font, 0xff000000);
        String s = "Minecraft " + SharedConstants.getCurrentVersion().getName();
        if (this.minecraft.isDemo()) {
            s = s + " Demo";
        } else {
            s = s + ("release".equalsIgnoreCase(
                this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
        }
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            s = s + I18n.get("menu.modded");
        }
        guiGraphics.drawString(minecraft.font, s, 2, height - 10, Colors.WHITE.get());
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderPanorama(@NotNull GuiGraphics guiGraphics, float partialTick) {
        if (Configs.clientConfig.SHOW_PANORAMA.get()) super.renderPanorama(guiGraphics, partialTick);
        else guiGraphics.fill(0, 0, width, height, Colors.BG_DARK.get());
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics ignored0, int ignored1, int ignored2, float ignored3) {
    }

    @Override
    public void onClose() {
        if (!Configs.clientConfig.CUSTOM_MENUS.get() || (minecraft != null && minecraft.isDemo())) super.onClose();
        else repositionElements();
    }

    private static void renderLogo(GuiGraphics guiGraphics, int screenWidth) {
        guiGraphics.blit(RenderType::guiTextured, MINECRAFT_LOGO, (screenWidth - LOGO_WIDTH) / 2, 30, 0.0F, 0.0F,
            LOGO_WIDTH, LOGO_HEIGHT, LOGO_TEXTURE_WIDTH, LOGO_TEXTURE_HEIGHT, Colors.WHITE.get());
    }
}
