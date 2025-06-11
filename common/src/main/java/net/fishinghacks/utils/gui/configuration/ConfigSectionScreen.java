package net.fishinghacks.utils.gui.configuration;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.TranslatableEnum;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigSpec;
import net.fishinghacks.utils.config.spec.RestartType;
import net.fishinghacks.utils.config.values.*;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.ListScreen;
import net.fishinghacks.utils.gui.PauseMenuScreen;
import net.fishinghacks.utils.gui.components.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigSectionScreen extends ListScreen {
    public static final Component CRUMB_SEPARATOR = Translation.GuiConfigCrumbSeperator.with()
        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    public static final Component GAME_RESTART_YES = Component.translatable("menu.quit"); // TitleScreen.init() et.al.
    public static final Component RETURN_TO_MENU = Component.translatable(
        "menu.returnToMenu"); // PauseScreen.RETURN_TO_MENU
    public static final Component IGNORE_TOOLTIP = Translation.GuiConfigRestartIgnoreTooltip.with()
        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

    protected final ConfigContext context;
    protected boolean changed;
    protected RestartType needsRestart = RestartType.None;
    protected final Map<String, ConfigSectionScreen> sectionCache = new HashMap<>();
    protected Button doneButton, undoButton, redoButton, resetButton;
    protected final UndoManager undoManager = new UndoManager();
    public boolean asPopup = false;

    public static void open(Minecraft mc, AbstractConfig config) {
        mc.setScreen(new ConfigSectionScreen(config, mc.screen));
    }

    public static ConfigSectionScreen openWithPath(Minecraft mc, AbstractConfig config, List<String> path) {
        var parent = mc.screen;
        var spec = config.spec();
        MutableComponent title = Translation.GuiConfigTitle.with();
        for (String key : path) {
            var elem = spec.elements.get(key);
            if (elem == null || !elem.isSubconfig()) break;
            spec = elem.asSubconfig();
            title = Translation.GuiConfigCrumbElement.with(title, CRUMB_SEPARATOR,
                Component.translatable(spec.getTranslationKey()));
        }
        var screen = new ConfigSectionScreen(new ConfigContext(parent, spec, config), title);
        mc.setScreen(screen);
        return screen;
    }

    public ConfigSectionScreen(Screen parent) {
        this(Configs.clientConfig, parent);
    }

    public ConfigSectionScreen(AbstractConfig config, Screen parent) {
        this(new ConfigContext(parent, config.spec(), config), Translation.GuiConfigTitle.get());
    }

    public ConfigSectionScreen(ConfigContext context, Component title) {
        super(title, context.parent());
        this.context = context;
    }

    protected boolean isAnyNonDefault() {
        for (final var entry : context.spec().elements.values())
            if (entry.isValue() && isNonDefault(entry.asCachedValue())) return true;
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (undoButton != null) undoButton.active = undoManager.canUndo();
        if (redoButton != null) redoButton.active = undoManager.canRedo();
        if (resetButton != null) resetButton.active = isAnyNonDefault();
        if (asPopup) titleWidget.setY(listStartY - titleWidget.getHeight());
        else titleWidget.setY((listStartY - titleWidget.getHeight()) / 2);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // title box
        if (asPopup) {
            guiGraphics.fill(titleWidget.getX() - 6, titleWidget.getY() - 6, titleWidget.getRight() + 6, listStartY,
                Colors.DARK.get());
            guiGraphics.fill(titleWidget.getX() - 4, titleWidget.getY() - 4, titleWidget.getRight() + 4, listStartY + 2,
                Colors.BG_DARK.get());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void resetValues() {
        List<UndoManager.Step<?>> list = new ArrayList<>();
        for (final var entry : context.spec().elements.entrySet()) {
            if (!entry.getValue().isValue()) continue;
            AbstractCachedValue value = entry.getValue().asCachedValue();
            if (!isNonDefault(value)) continue;
            String key = entry.getKey();
            list.add(undoManager.step(v -> {
                value.set(v);
                onChanged(key);
            }, value.getDefault(), v -> {
                value.set(v);
                onChanged(key);
            }, value.getRaw()));
        }
        undoManager.add(list);
        rebuildList();
    }

    protected boolean isNonDefault(AbstractCachedValue<?> cv) {
        return !Objects.equals(cv.getRaw(), cv.getDefault());
    }

    @Override
    protected void onInit() {
        int y = listStartY;
        int x = listStartX - Button.CUBE_WIDTH - 6;

        doneButton = this.addRenderableWidget(
            Button.Builder.cube("<").onPress((button) -> this.onClose()).pos(x, y).build());
        y += Button.DEFAULT_HEIGHT + 6;
        resetButton = this.addRenderableWidget(
            new IconButton.Builder(Icons.RESET).pos(x, y).onPress(btn -> resetValues()).build());
        y += Button.DEFAULT_HEIGHT + 6;
        undoButton = this.addRenderableWidget(new IconButton.Builder(Icons.UNDO).pos(x, y).onPress(btn -> {
            undoManager.undo();
            rebuildList();
        }).build());
        y += Button.DEFAULT_HEIGHT + 6;
        redoButton = this.addRenderableWidget(new IconButton.Builder(Icons.REDO).pos(x, y).onPress(btn -> {
            undoManager.redo();
            rebuildList();
        }).build());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void buildList() {
        listLayout = new LinearLayout(listStartX, listStartY, LinearLayout.Orientation.VERTICAL);
        LinearLayout boxLayout = LinearLayout.vertical();
        listLayout.addChild(new Box(boxLayout));

        var entries = context.spec().elements.entrySet();
        final List<Element> elements = new ArrayList<>(entries.size());
        for (final var entry : entries) {
            final String key = entry.getKey();
            if (key.startsWith("_")) continue;
            if (entry.getValue().isSubconfig()) {
                elements.add(createSection(key, entry.getValue().asSubconfig()));
                continue;
            }
            if (!entry.getValue().isValue()) {
                elements.add(null);
                continue;
            }
            AbstractCachedValue value = entry.getValue().asCachedValue();
            switch (value) {
                case CachedColorValue colorValue -> elements.add(createColorValue(key, colorValue));
                case MufflerState val -> elements.add(createMufflerState(val));
                case MufflerStateProxy val -> elements.add(createMufflerState(val));
                /// Note: this will never be handled as it is server side. connect to the service server and use the
                /// cosmetics menu or edit the config yourself.
                case CosmeticMapConfigValue ignored -> elements.add(null);
                default -> {
                    Object defaultVal = value.getDefault();
                    elements.add(switch (defaultVal) {
                        case String ignored -> createStringValue(key, value);
                        case Boolean ignored -> createBooleanValue(key, value);
                        case Integer ignored -> createIntegerValue(key, value);
                        case Double ignored -> createDoubleValue(key, value);
                        case Long ignored -> createLongValue(key, value);
                        case Enum<?> ignored -> createEnumValue(key, value, (Class<Enum>) defaultVal.getClass());
                        default -> createOtherValue(value);
                    });
                }
            }
        }

        int width = listWidth - 2 * Box.DEFAULT_BORDER_SIZE;
        for (var element : elements) {
            if (element == null) continue;
            MutableComponent name = element.name == null ? Component.empty() : element.name.copy();
            MutableComponent tooltip = element.description == null ? null : element.description.copy();
            boxLayout.addChild(new ConfigSection(5, 0, 0, name, tooltip, element.widget, width));
        }

        boxLayout.arrangeElements();
        listLayout.arrangeElements();
    }

    private Element createColorValue(final String key, CachedColorValue value) {
        var name = value.getNameTranslation();
        final ColorInput box = new ColorInput(
            new Input.Builder(font, 0, 0, Input.DEFAULT_WIDTH_BIG, Input.DEFAULT_HEIGHT, name).editable(true)
                .maxLength(9).build(), value.getRaw());
        box.setResponder(newValue -> {
            if (newValue == null || !value.isValid(newValue)) {
                box.setTextColor(0xffff0000);
                return;
            }
            box.setTextColor(Input.DEFAULT_TEXT_COLOR);
            if (newValue.equals(value.getRaw())) return;
            undoManager.add(new UndoAction<>(value::set, key, this::onChanged), newValue, value.getRaw());
        });

        return new Element(name, value.getTooltipTranslation(), box);
    }

    private Element createMufflerState(AbstractCachedValue<?> value) {
        var name = value.getNameTranslation();

        return new Element(name, value.getTooltipTranslation(),
            Button.Builder.normal(Translation.GuiConfigSectionButton.get())
                .onPress(ignored -> {
                    var screen = new MufflerScreen(this);
                    screen.asPopup = this.asPopup;
                    Minecraft.getInstance().setScreen(screen);
                }).build());
    }

    private Element createStringValue(final String key, AbstractCachedValue<String> value) {
        var val = value.getRaw();
        if (val.length() > 192) {
            final Text label = new Text.Builder(Input.DEFAULT_WIDTH_BIG, Input.DEFAULT_HEIGHT,
                Component.literal(val.substring(0, 128)), font).build();
            label.setTooltip(Tooltip.create(Translation.GuiConfigTooLong.get()));
            return new Element(value.getNameTranslation(), value.getTooltipTranslation(), label);
        }
        var name = value.getNameTranslation();
        final Input box = new Input.Builder(font, 0, 0, Input.DEFAULT_WIDTH_BIG, Input.DEFAULT_HEIGHT, name).editable(
            true).maxLength(Math.clamp(val.length() + 5, 128, 192)).value(val).build();
        box.setResponder(newValue -> {
            if (newValue == null || !value.isValid(newValue)) {
                box.setTextColor(0xffff0000);
                return;
            }
            box.setTextColor(Input.DEFAULT_TEXT_COLOR);
            if (newValue.equals(value.getRaw())) return;
            undoManager.add(new UndoAction<>(value::set, key, this::onChanged), newValue, value.getRaw());
        });

        return new Element(name, value.getTooltipTranslation(), box);
    }

    private Element createBooleanValue(final String key, AbstractCachedValue<Boolean> value) {
        final Toggle toggle = new Toggle.Builder().checked(value.getRaw()).onChange(
            (ignored, newValue) -> undoManager.add(new UndoAction<>(value::set, key, this::onChanged), newValue,
                value.getRaw())).build();

        return new Element(value.getNameTranslation(), value.getTooltipTranslation(), toggle);
    }

    private <T extends Enum<T>> Element createEnumValue(final String key, AbstractCachedValue<T> value,
                                                        Class<T> clazz) {
        assert clazz != null;
        final List<T> list = Arrays.stream(clazz.getEnumConstants()).filter(value::isValid).toList();
        final GuiDropdown<T> dropdown = new GuiDropdown<>(value.getRaw(),
            displayValue -> displayValue instanceof TranslatableEnum translatableEnum ?
                translatableEnum.getTranslatedName() : Component.literal(
                displayValue.name()), (ignored, newValue) -> {
            if (value.getRaw().equals(newValue)) return;
            undoManager.add(new UndoAction<>(value::set, key, this::onChanged), newValue, value.getRaw());
        }, list);

        return new Element(value.getNameTranslation(), value.getTooltipTranslation(), dropdown);
    }

    private Element createSection(final String key, final ConfigSpec subsection) {
        if (subsection.elements.isEmpty()) return null;
        var translationKey = subsection.getTranslationKey();
        Button button = Button.Builder.normal(Component.translatable(
                TranslationChecker.getWithFallback(translationKey + ".button",
                    Translation.GuiConfigSectionButton.key())))
            .onPress(ignored -> {
                if (minecraft == null) return;
                minecraft.setScreen(sectionCache.computeIfAbsent(key,
                    k -> new ConfigSectionScreen(new ConfigContext(this, subsection, context.config()),
                        Translation.GuiConfigCrumbElement.with(getTitle(), CRUMB_SEPARATOR,
                            Component.translatable(translationKey)))));
            }).build();

        return new Element(Component.translatable(translationKey).append("..."),
            TranslationChecker.exists(translationKey + ".tooltip") ? Component.translatable(
                translationKey + ".tooltip") : null, button);
    }

    private Element createOtherValue(final AbstractCachedValue<?> value) {
        final Text label = new Text.Builder(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT,
            Component.literal(Objects.toString(value.getRaw())), font).build();
        label.setTooltip(Tooltip.create(Translation.GuiConfigUnsupported.get()));
        return new Element(value.getNameTranslation(), value.getTooltipTranslation(), label);
    }

    private Element createIntegerValue(final String key, AbstractCachedValue<Integer> value) {
        return createNumberBox(key, value, Integer::decode);
    }

    private Element createLongValue(final String key, AbstractCachedValue<Long> value) {
        return createNumberBox(key, value, Long::decode);
    }

    private Element createDoubleValue(final String key, AbstractCachedValue<Double> value) {
        return createNumberBox(key, value, Double::parseDouble);
    }

    private <T extends Number & Comparable<? super T>> Element createNumberBox(final String key,
                                                                               final AbstractCachedValue<T> value,
                                                                               final Function<String, T> parser) {
        var name = value.getNameTranslation();
        final Input box = new Input.Builder(font, 0, 0, Input.DEFAULT_WIDTH_BIG, Input.DEFAULT_HEIGHT, name).editable(
            true).filter(str -> {
            try {
                parser.apply(str);
                return true;
            } catch (final NumberFormatException e) {
                return isPartialNumber(str, true);
            }
        }).value(value.getRaw() + "").build();
        box.setResponder(str -> {
            try {
                final T newValue = parser.apply(str);
                if (newValue != null && value.isValid(newValue)) {
                    box.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
                    if (!newValue.equals(value.getRaw()))
                        undoManager.add(new UndoAction<>(value::set, key, this::onChanged), newValue, value.getRaw());
                    return;
                }
            } catch (final NumberFormatException e) {
                // field probably is just empty/partial, ignore that
            }
            box.setTextColor(0xFFFF0000);
        });
        return new Element(name, value.getTooltipTranslation(), box);
    }

    private boolean isPartialNumber(String value, boolean allowNegative) {
        return switch (value) {
            case "", "0x", "0X", "0" -> true;
            case "#" -> true; // not valid for doubles, but not worth making a special case
            case "-", "-0", "-0x", "-0X" -> allowNegative;
            // case "-#" -> allowNegative; // Java allows this, but no, thanks, that's just cursed.
            // doubles can also do NaN, inf, and 0e0. Again, not worth making a special case for those, I say.
            default -> false;
        };
    }

    protected void onChanged(final String key) {
        changed = true;
        final var valueSpec = context.spec().elements.get(key);
        if (valueSpec != null && valueSpec.isValue())
            needsRestart = needsRestart.with(valueSpec.asCachedValue().needsRestart);
    }

    @Override
    public void onClose() {
        if (!changed) {
            super.onClose();
            return;
        }
        if (parent instanceof ConfigSectionScreen screen) {
            // "bubble up" the marker so the top-most section can change the ModConfig
            screen.changed = true;
            screen.needsRestart = screen.needsRestart.with(needsRestart);
            super.onClose();
            return;
        }
        // we are a top-level per-type config screen, i.e. one specific config file. Save the config and tell
        // the mod to reload.
        context.config().save();
        assert minecraft != null;
        switch (needsRestart) {
            case None -> super.onClose();
            case Game -> minecraft.setScreen(new TooltipConfirmScreen(b -> {
                if (b) minecraft.stop();
                else super.onClose();
            }, Translation.GuiConfigRestartGameTitle.get(), Translation.GuiConfigRestartGameDescription.get(),
                GAME_RESTART_YES, Translation.GuiConfigRestartIgnore.get()));
            case World -> {
                if (minecraft.level == null) {
                    super.onClose();
                    return;
                }
                minecraft.setScreen(new TooltipConfirmScreen(b -> {
                    if (b) PauseMenuScreen.disconnect();
                    else super.onClose();
                }, Translation.GuiConfigRestartWorldTitle.get(), Translation.GuiConfigRestartWorldDescription.get(),
                    minecraft.isLocalServer() ? RETURN_TO_MENU : CommonComponents.GUI_DONE,
                    Translation.GuiConfigRestartIgnore.get()));
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!asPopup || parent == null) {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }
        parent.render(guiGraphics, -1, -1, partialTick);
        assert this.minecraft != null;
        guiGraphics.flush();
        RenderSystem.getDevice().createCommandEncoder()
            .clearDepthTexture(Objects.requireNonNull(this.minecraft.getMainRenderTarget().getDepthTexture()), 1.0);
        renderTransparentBackground(guiGraphics);
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
                button.setTooltip(Tooltip.create(IGNORE_TOOLTIP));
            } else {
                seenYes = true;
            }
            super.addExitButton(button);
        }
    }

    public record Element(Component name, @Nullable Component description, AbstractWidget widget) {
    }

    private record UndoAction<T>(Consumer<T> target, String key, Consumer<String> onChange) implements Consumer<T> {
        @Override
        public void accept(T v) {
            target.accept(v);
            onChange.accept(key);
        }
    }
}
