package net.fishinghacks.utils.client.gui.configuration;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fishinghacks.utils.client.gui.ListScreen;
import net.fishinghacks.utils.client.gui.PauseMenuScreen;
import net.fishinghacks.utils.client.gui.components.Box;
import net.fishinghacks.utils.client.gui.components.Button;
import net.fishinghacks.utils.client.gui.components.Center;
import net.fishinghacks.utils.client.gui.components.Spacer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class ConfigurationSelectionScreen extends ListScreen {
    // source: net.neoforged.neoforge.client.gui.ConfigurationScreen
    private static final String LANG_PREFIX = "neoforge.configuration.uitext.";

    public static final Component TOOLTIP_CANNOT_EDIT_THIS_WHILE_ONLINE = Component.translatable(
        LANG_PREFIX + "notonline").withStyle(ChatFormatting.RED);
    public static final Component TOOLTIP_CANNOT_EDIT_THIS_WHILE_OPEN_TO_LAN = Component.translatable(
        LANG_PREFIX + "notlan").withStyle(ChatFormatting.RED);
    public static final Component TOOLTIP_CANNOT_EDIT_NOT_LOADED = Component.translatable(LANG_PREFIX + "notloaded")
        .withStyle(ChatFormatting.RED);
    private static final String SECTION = LANG_PREFIX + "section";
    private static final MutableComponent EMPTY_LINE = Component.literal("\n\n");
    private static final String FILENAME_TOOLTIP = LANG_PREFIX + "filenametooltip";
    public static final Component GAME_RESTART_TITLE = Component.translatable(LANG_PREFIX + "restart.game.title");
    public static final Component GAME_RESTART_MESSAGE = Component.translatable(LANG_PREFIX + "restart.game.text");
    public static final Component GAME_RESTART_YES = Component.translatable("menu.quit"); // TitleScreen.init() et.al.
    public static final Component SERVER_RESTART_TITLE = Component.translatable(LANG_PREFIX + "restart.server.title");
    public static final Component SERVER_RESTART_MESSAGE = Component.translatable(LANG_PREFIX + "restart.server.text");
    public static final Component RETURN_TO_MENU = Component.translatable(
        "menu.returnToMenu"); // PauseScreen.RETURN_TO_MENU
    public static final Component RESTART_NO = Component.translatable(LANG_PREFIX + "restart.return");
    public static final Component RESTART_NO_TOOLTIP = Component.translatable(LANG_PREFIX + "restart.return.tooltip")
        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);


    protected final ModContainer mod;
    private boolean autoClose = false;
    ModConfigSpec.RestartType needsRestart = ModConfigSpec.RestartType.NONE;

    public ConfigurationSelectionScreen(final ModContainer mod, final Screen parent) {
        super(
            Component.translatable(TranslationChecker.getWithFallback(mod.getModId() + ".title", LANG_PREFIX + "title"),
                mod.getModInfo().getDisplayName()), parent);
        this.mod = mod;
    }

    protected Component translateConfig(ModConfig cfg, String suffix, String fallback) {
        String key = mod.getModId() + ".configuration.section." + cfg.getFileName().replaceAll("[^a-zA-Z0-9]+", ".")
            .replaceFirst("^\\.", "").replaceFirst("\\.$", "").toLowerCase(Locale.ENGLISH) + suffix;
        return Component.translatable(TranslationChecker.getWithFallback(key, fallback),
            mod.getModInfo().getDisplayName());
    }

    @Override
    protected void onInit() {
        if (autoClose) this.onClose();
    }

    @Override
    protected void buildList() {
        if (autoClose) return;
        if (minecraft == null) return;
        int centerWidth = width / 4 * 3;
        Box box = this.addRenderableOnly(
            new Box(new Spacer(centerWidth, listVisibleHeight), new Box.Borders().setBottom(false)));
        box.setY(listStartY);
        box.setX(listStartX);
        box.setWidth(centerWidth);
        box.setHeight(listVisibleHeight);

        listLayout.addChild(new Spacer(0, 10));
        Button lastButton = null;
        int count = 0;
        for (final ModConfig.Type type : ModConfig.Type.values()) {
            for (final ModConfig cfg : ModConfigs.getConfigSet(type)) {
                if (!cfg.getModId().equals(mod.getModId())) continue;
                Button btn = Button.Builder.big(Component.translatable(SECTION,
                        translateConfig(cfg, "",
                            LANG_PREFIX + "type." + cfg.getType().name().toLowerCase(Locale.ROOT))))
                    .onPress(ignored -> {
                        var screen = new ConfigurationSectionScreen(ConfigContext.top(mod.getModId(), this, cfg),
                            translateConfig(cfg, ".title",
                                LANG_PREFIX + "title." + type.name().toLowerCase(Locale.ROOT)));
                        minecraft.setScreen(screen);
                    }).build();
                MutableComponent tooltip = Component.empty();
                if (!((ModConfigSpec) cfg.getSpec()).isLoaded()) {
                    tooltip.append(TOOLTIP_CANNOT_EDIT_NOT_LOADED).append(EMPTY_LINE);
                    btn.active = false;
                } else if (type == ModConfig.Type.SERVER && minecraft.getCurrentServer() != null && !minecraft.isSingleplayer()) {
                    tooltip.append(TOOLTIP_CANNOT_EDIT_THIS_WHILE_ONLINE).append(EMPTY_LINE);
                    btn.active = false;
                } else if (type == ModConfig.Type.SERVER && minecraft.isLocalServer() && minecraft.getSingleplayerServer() != null && minecraft.getSingleplayerServer()
                    .isPublished()) {
                    tooltip.append(TOOLTIP_CANNOT_EDIT_THIS_WHILE_OPEN_TO_LAN).append(EMPTY_LINE);
                    btn.active = false;
                }
                tooltip.append(
                    Component.translatable(FILENAME_TOOLTIP, cfg.getFileName()).withStyle(ChatFormatting.GRAY));
                btn.setTooltip(Tooltip.create(tooltip));
                listLayout.addChild(new Center(0, 0, centerWidth, 0, btn));
                if (btn.active) {
                    count++;
                    lastButton = btn;
                }
            }
        }

        if (count == 1) {
            autoClose = true;
            lastButton.onPress();
        }
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        switch (this.needsRestart) {
            case GAME -> {
                minecraft.setScreen(new TooltipConfirmScreen(b -> {
                    if (b) minecraft.stop();
                    else super.onClose();
                }, GAME_RESTART_TITLE, GAME_RESTART_MESSAGE, GAME_RESTART_YES, RESTART_NO));
                return;
            }
            case WORLD -> {
                if (minecraft.level != null) {
                    minecraft.setScreen(new TooltipConfirmScreen(b -> {
                        if (b) PauseMenuScreen.disconnect();
                        else super.onClose();
                    }, SERVER_RESTART_TITLE, SERVER_RESTART_MESSAGE,
                        minecraft.isLocalServer() ? RETURN_TO_MENU : CommonComponents.GUI_DONE, RESTART_NO));
                    return;
                }
            }
        }
        super.onClose();
    }

    private static final class TooltipConfirmScreen extends ConfirmScreen {
        boolean seenYes = false;

        private TooltipConfirmScreen(BooleanConsumer callback, Component title, Component message, Component yesButton,
                                     Component noButton) {
            super(callback, title, message, yesButton, noButton);
        }

        @Override
        protected void init() {
            seenYes = false;
            super.init();
        }

        @Override
        protected void addExitButton(@NotNull net.minecraft.client.gui.components.Button button) {
            if (seenYes) {
                button.setTooltip(Tooltip.create(RESTART_NO_TOOLTIP));
            } else {
                seenYes = true;
            }
            super.addExitButton(button);
        }
    }
}
