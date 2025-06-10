package net.fishinghacks.utils.gui.mcsettings;

import net.fishinghacks.utils.TranslatableEnum;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.ListScreen;
import net.fishinghacks.utils.gui.components.*;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class McSettingsScreen extends ListScreen {
    private static final Component TITLE = Component.translatable("options.title");
    private static final Component FOV = Component.translatable("options.fov");
    private static final Component FOV_MIN = Options.genericValueLabel(FOV, Component.translatable("options.fov.min"));
    private static final Component FOV_MAX = Options.genericValueLabel(FOV, Component.translatable("options.fov.max"));
    private final Options options;
    OptionType current;
    private @Nullable StringWidget title;
    private @Nullable OptionSubscreen subscreen = null;
    @Nullable Input keybindSearch = null;
    @Nullable Button keybindResetButton = null;

    public McSettingsScreen(Screen parent, Options options, OptionType optionType) {
        super(TITLE, parent);
        this.options = options;
        this.current = optionType;
    }

    public McSettingsScreen(Screen parent, Options options) {
        this(parent, options, OptionType.Sounds);
    }

    @Override
    protected int getListWidth() {
        int width = this.width / 4;
        if (width > Button.BIG_WIDTH + 18) return width * 3;
        width = this.width / 3;
        if (width > Button.BIG_WIDTH + 18) return width * 2;
        return Math.max(this.width - Button.BIG_WIDTH - 30, 0);
    }

    @Override
    protected int getListStartX() {
        int spaceLeftOver = width - listWidth - Button.BIG_WIDTH - 6;
        // center the list and the buttons
        return Button.BIG_WIDTH + 6 + spaceLeftOver / 2;
    }

    @Override
    protected @Nullable AbstractWidget addTitle() {
        return title = new StringWidget(current.title, font);
    }

    @Override
    protected void onInit() {
        assert minecraft != null;
        int y = this.listStartY;
        int x = this.listStartX - 6 - Button.BIG_WIDTH;
        final int fovX = x + Button.CUBE_WIDTH + 6;
        if (title != null) title.setX(listStartX + (listWidth - title.getWidth()) / 2);

        this.addRenderableWidget(Button.Builder.cube("<").pos(x, y).onPress(ignored -> this.onClose()).build());
        if (current == OptionType.Keybinds) {
            final int searchWidth = Button.BIG_WIDTH - 12 - Button.CUBE_WIDTH * 2;
            keybindSearch = this.addRenderableWidget(
                Input.Builder.small(fovX, y).size(searchWidth, Button.DEFAULT_HEIGHT).hint(Translation.Search.get())
                    .responder(ignored -> {
                        if (current == OptionType.Keybinds) rebuildList();
                    }).build());
            keybindResetButton = this.addRenderableWidget(
                new IconButton.Builder(Icons.RESET).pos(fovX + searchWidth + 6, y).onPress(this::resetKeybinds)
                    .build());
        } else if (current == OptionType.ResourcePacks) this.addRenderableWidget(
            new IconButton.Builder(Icons.FOLDER).pos(fovX, y)
                .onPress(ignored -> Util.getPlatform().openPath(minecraft.getResourcePackDirectory())).build());
        else {
            this.addRenderableWidget(
                createFovSliderOrDifficultyDropdown(fovX, y, Button.BIG_WIDTH - Button.CUBE_WIDTH - 6));
            keybindSearch = null;
            keybindResetButton = null;
        }
        if (minecraft.level != null) {
            y += Button.DEFAULT_HEIGHT + 6;
            this.addRenderableWidget(getDifficultyGuiDropdown(x, y));
        }
        y += 2 * Button.DEFAULT_HEIGHT + 6;

        for (OptionType ty : OptionType.values()) y += addOptionButton(x, y, ty);
        addTelemetryButton(x, y);
    }

    @Override
    protected void buildList() {
        if (subscreen == null) subscreen = current.getSubscreen(minecraft, options);
        if (minecraft != null && minecraft.level != null) {
            subscreen.addElements(listLayout, options, listWidth, this);
            return;
        }

        LinearLayout layout = LinearLayout.vertical();
        listLayout.addChild(new Box(layout));
        int width = listWidth - Box.DEFAULT_BORDER_SIZE * 2;
        subscreen.addElements(layout, options, width, this);

        layout.arrangeElements();
    }

    public AbstractWidget createFovSliderOrDifficultyDropdown(int x, int y, int width) {
        assert minecraft != null;

        OptionInstance.IntRange range = (OptionInstance.IntRange) this.options.fov().values();
        int fov = this.options.fov().get();
        var slider = new Slider(range.minInclusive(), range.maxInclusive(), fov, fovFromValue(fov));
        slider.onChange((s, value) -> {
            s.setMessage(fovFromValue(value));
            this.options.fov().set(value);
        });
        slider.setPosition(x, y);
        slider.setWidth(width);

        return slider;
    }

    private @NotNull GuiDropdown<Difficulty> getDifficultyGuiDropdown(int x, int y) {
        assert minecraft != null;
        assert minecraft.level != null;

        GuiDropdown<Difficulty> dropdown = new GuiDropdown<>(minecraft.level.getDifficulty(),
            difficulty -> CommonComponents.optionNameValue(Translations.DIFFICULTY, difficulty.getDisplayName()),
            Difficulty.values());
        dropdown.setTooltip(Tooltip.create(minecraft.level.getDifficulty().getInfo()));
        dropdown.setPosition(x, y);
        dropdown.setWidth(Button.BIG_WIDTH);
        dropdown.onValueChange((ignored, difficulty) -> {
            var connection = minecraft.getConnection();
            if (connection != null) {
                connection.send(new ServerboundChangeDifficultyPacket(difficulty));
                dropdown.setTooltip(Tooltip.create(difficulty.getInfo()));
            } else this.repositionElements();
        });
        return dropdown;
    }

    private static Component fovFromValue(int fov) {
        return switch (fov) {
            case 70 -> FOV_MIN;
            case 110 -> FOV_MAX;
            default -> Options.genericValueLabel(FOV, Component.literal("" + fov));
        };
    }

    public void resetKeybinds(Button ignored) {
        for (KeyMapping keymapping : this.options.keyMappings) {
            keymapping.setKey(keymapping.getDefaultKey());
        }
        this.rebuildList();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft != null && minecraft.level == null) {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        this.renderTransparentBackground(guiGraphics);
    }

    private int addOptionButton(int x, int y, OptionType ty) {
        assert minecraft != null;
        Button button = Button.Builder.big(ty.name).onPress(ignored -> {
            if (current == ty) return;
            // this is fine because we will call rebuildList after this before the next render
            this.scrollHeight = 0;
            if (current == OptionType.Keybinds || ty == OptionType.Keybinds || current == OptionType.ResourcePacks || ty == OptionType.ResourcePacks) {
                this.current = ty;
                if (subscreen != null) subscreen.onClose(options, this);
                subscreen = null;
                this.repositionElements();
                return;
            }
            if (subscreen != null) subscreen.onClose(options, this);
            subscreen = null;
            this.current = ty;
            this.rebuildList();
            if (title == null) return;
            title.setMessage(current.title);
            title.setWidth(font.width(current.title));
            title.setX(listStartX + (listWidth - title.getWidth()) / 2);
        }).pos(x, y).build();
        return this.addRenderableWidget(button).getHeight() + 6;
    }

    private void addTelemetryButton(int x, int y) {
        assert minecraft != null;
        if (!minecraft.allowsTelemetry()) return;
        this.addRenderableWidget(Button.Builder.big(Translations.TELEMETRY)
            .onPress(ignored -> minecraft.setScreen(new TelemetryInfoScreen(this, options))).pos(x, y).build());
    }

    @Override
    public void removed() {
        if (this.subscreen != null) subscreen.onClose(options, this);
        this.options.save();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.subscreen != null) this.subscreen.render(guiGraphics, mouseX, mouseY, partialTick, this);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.subscreen != null && this.subscreen.keyPressed(keyCode, scanCode, modifiers, this)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.subscreen != null && this.subscreen.keyReleased(keyCode, scanCode, modifiers, this)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.subscreen != null && this.subscreen.mouseClicked(mouseX, mouseY, button, this)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public enum OptionType implements TranslatableEnum {
        SkinCustomization(Translations.SKIN_CUSTOMIZATION, Translations.SKIN_CUSTOMIZATION_TITLE), Video(
            Translations.VIDEO, Translations.VIDEO_TITLE), Language(Translations.LANGUAGE,
            Translations.LANGUAGE_TITLE), ResourcePacks(Translations.RESOURCEPACK,
            Translations.RESOURCEPACK_TITLE), Sounds(Translations.SOUNDS, Translations.SOUNDS_TITLE), Controls(
            Translations.CONTROLS, Translations.CONTROLS_TITLE), Keybinds(Translations.KEYBINDS,
            Translations.KEYBINDS_TITLE), Chat(Translations.CHAT, Translations.CHAT_TITLE), Accessibility(
            Translations.ACCESSIBILITY, Translations.ACCESSIBILITY_TITLE), Online(Translations.ONLINE,
            Translations.ONLINE_TITLE);

        private final Component name;
        private final Component title;

        OptionType(Component name, Component title) {
            this.name = name;
            this.title = title;
        }

        @Override
        public @NotNull Component getTranslatedName() {
            return name;
        }

        public OptionSubscreen getSubscreen(Minecraft mc, Options options) {
            return switch (this) {
                case SkinCustomization -> new SkinCustomizations();
                case Video -> new VideoSettings();
                case Keybinds -> new Keybinds();
                case Sounds -> new Sounds();
                case Accessibility -> new Accessibility();
                case Online -> new Online();
                case Chat -> new Chat();
                case Controls -> new Controls();
                case Language -> new Languages();
                case ResourcePacks -> new ResourcePacks(options, mc);
            };
        }
    }
}