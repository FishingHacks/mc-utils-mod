package net.fishinghacks.utils.gui.configuration;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.ListScreen;
import net.fishinghacks.utils.gui.components.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigurationSectionScreen extends ListScreen {
    protected static final long MAX_SLIDER_SIZE = 256L;
    private static final String RANGE_TOOLTIP = "neoforge.configuration.uitext.rangetooltip";
    public static final Component LONG_STRING = Component.translatable("neoforge.configuration.uitext.longstring")
        .withStyle(ChatFormatting.RED);
    public static final Component UNSUPPORTED_ELEMENT = Component.translatable(
        "neoforge.configuration.uitext.unsupportedelement").withStyle(ChatFormatting.RED);
    private static final String SECTION = "neoforge.configuration.uitext.section";
    private static final String SECTION_TEXT = "neoforge.configuration.uitext.sectiontext";
    public static final Component CRUMB_SEPARATOR = Component.translatable(
        "neoforge.configuration.uitext.breadcrumb.separator").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    private static final String CRUMB = "neoforge.configuration.uitext.breadcrumb.order";

    protected final ConfigContext context;
    protected boolean changed;
    protected ModConfigSpec.RestartType needsRestart = ModConfigSpec.RestartType.NONE;
    protected final Map<String, ConfigurationSectionScreen> sectionCache = new HashMap<>();
    protected Button doneButton, undoButton, redoButton, resetButton;
    protected final UndoManager undoManager = new UndoManager();

    protected ConfigurationSectionScreen(ConfigContext context, Component title) {
        super(title, context.parent());
        this.context = context;
    }

    protected boolean isAnyNondefault() {
        for (final UnmodifiableConfig.Entry entry : context.entries()) {
            if (entry.getRawValue() instanceof final ModConfigSpec.ConfigValue<?> cv) {
                if (!(getValueSpec(entry.getKey()) instanceof ModConfigSpec.ListValueSpec) && isNonDefault(cv)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (undoButton != null) undoButton.active = undoManager.canUndo();
        if (redoButton != null) redoButton.active = undoManager.canRedo();
        if (resetButton != null) resetButton.active = isAnyNondefault();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void resetValues() {
        List<UndoManager.Step<?>> list = new ArrayList<>();
        for (final UnmodifiableConfig.Entry entry : context.entries()) {
            if (entry.getRawValue() instanceof final ModConfigSpec.ConfigValue cv && !(getValueSpec(
                entry.getKey()) instanceof ModConfigSpec.ListValueSpec) && isNonDefault(cv)) {
                final String key = entry.getKey();
                //noinspection DataFlowIssue
                list.add(undoManager.step(v -> {
                    cv.set(v);
                    onChanged(key);
                }, getValueSpec(key).correct(null), v -> {
                    cv.set(v);
                    onChanged(key);
                }, cv.getRaw()));
            }
        }
        undoManager.add(list);
        rebuildList();
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

        final List<Element> elements = new ArrayList<>();
        for (final UnmodifiableConfig.Entry entry : context.entries()) {
            final String key = entry.getKey();
//            if(key.startsWith("_")) continue;
            final Object rawValue = entry.getRawValue();
            switch (rawValue) {
                case ModConfigSpec.ConfigValue cv -> {
                    var valueSpec = getValueSpec(key);
                    Element element = switch (valueSpec) {
                        case ModConfigSpec.ListValueSpec ignored -> null;
                        case
                            ModConfigSpec.ValueSpec spec when cv.getClass() == ModConfigSpec.ConfigValue.class && spec.getDefault() instanceof String ->
                            createStringValue(key, valueSpec::test, (Supplier) cv::getRaw, (Consumer) cv::set);
                        case
                            ModConfigSpec.ValueSpec spec when cv.getClass() == ModConfigSpec.ConfigValue.class && spec.getDefault() instanceof Integer ->
                            createIntegerValue(key, valueSpec, (Supplier) cv::getRaw, (Consumer) cv::set);
                        case
                            ModConfigSpec.ValueSpec spec when cv.getClass() == ModConfigSpec.ConfigValue.class && spec.getDefault() instanceof Long ->
                            createLongValue(key, valueSpec, (Supplier) cv::getRaw, (Consumer) cv::set);
                        case
                            ModConfigSpec.ValueSpec spec when cv.getClass() == ModConfigSpec.ConfigValue.class && spec.getDefault() instanceof Double ->
                            createDoubleValue(key, valueSpec, (Supplier) cv::getRaw, (Consumer) cv::set);
                        case
                            ModConfigSpec.ValueSpec spec when cv.getClass() == ModConfigSpec.ConfigValue.class && spec.getDefault() instanceof Enum<?> ->
                            createEnumValue(key, valueSpec, (Supplier) cv::getRaw, (Consumer) cv::set);
                        case null -> null;

                        default -> switch (cv) {
                            case ModConfigSpec.BooleanValue value -> createBooleanValue(key, value::getRaw, value::set);
                            case ModConfigSpec.IntValue value ->
                                createIntegerValue(key, valueSpec, value::getRaw, value::set);
                            case ModConfigSpec.LongValue value ->
                                createLongValue(key, valueSpec, value::getRaw, value::set);
                            case ModConfigSpec.DoubleValue value ->
                                createDoubleValue(key, valueSpec, value::getRaw, value::set);
                            case ModConfigSpec.EnumValue value ->
                                createEnumValue(key, valueSpec, (Supplier) value::getRaw, (Consumer) value::set);
                            default -> createOtherValue(key, cv);
                        };
                    };
                    elements.add(element);
                }
                case UnmodifiableConfig subsection when context.valueSpecs()
                    .get(key) instanceof UnmodifiableConfig subconfig ->
                    elements.add(createSection(key, subconfig, subsection));
                default -> elements.add(null);
            }
        }

        int width = listWidth - 2 * Box.DEFAULT_BORDER_SIZE;
        for (var element : elements) {
            if (element == null) continue;
            MutableComponent name = element.name == null ? Component.empty() : element.name.copy();
            MutableComponent tooltip = element.tooltip == null ? null : element.tooltip.copy();
            boxLayout.addChild(new ConfigSection(5, 0, 0, name, tooltip, element.widget, width));
        }

        boxLayout.arrangeElements();
        listLayout.arrangeElements();
    }

    protected void onChanged(final String key) {
        changed = true;
        final ModConfigSpec.ValueSpec valueSpec = getValueSpec(key);
        if (valueSpec != null) {
            needsRestart = needsRestart.with(valueSpec.restartType());
        }
    }

    protected boolean isNonDefault(ModConfigSpec.ConfigValue<?> cv) {
        return !Objects.equals(cv.getRaw(), cv.getDefault());
    }

    @Nullable
    protected ModConfigSpec.ValueSpec getValueSpec(final String key) {
        final Object object = context.valueSpecs().get(key);
        if (object instanceof final ModConfigSpec.ValueSpec vs) {
            return vs;
        } else {
            return null;
        }
    }

    protected String getTranslationKey(final String key) {
        final ModConfigSpec.ValueSpec spec = getValueSpec(key);
        final String result = spec != null ? spec.getTranslationKey() : context.modSpec()
            .getLevelTranslationKey(context.makeKeyList(key));
        return result != null ? result : context.modId() + ".configuration." + key;
    }

    protected MutableComponent getTranslation(final String key) {
        return Component.translatable(getTranslationKey(key));
    }

    protected String getComment(final String key) {
        final ModConfigSpec.ValueSpec valueSpec = getValueSpec(key);
        return valueSpec != null ? valueSpec.getComment() : context.modSpec().getLevelComment(context.makeKeyList(key));
    }

    protected @Nullable Component getTooltipTranslation(final String key, @Nullable ModConfigSpec.Range<?> range) {
        final String tooltipKey = getTranslationKey(key) + ".tooltip";
        final String comment = getComment(key);
        final boolean hasTranslatedTooltip = I18n.exists(tooltipKey);
        @Nullable MutableComponent component = hasTranslatedTooltip || !Strings.isBlank(
            comment) ? Component.translatableWithFallback(tooltipKey, comment) : null;
        if (range != null) {
            MutableComponent rangeComponent = Component.translatable(RANGE_TOOLTIP, range.toString())
                .withStyle(ChatFormatting.GRAY);
            if (component == null) component = rangeComponent;
            else component = component.append(" (").append(rangeComponent).append(")");
        }
        return component;
    }


    private Element createStringValue(final String key, final Predicate<String> tester, final Supplier<String> source,
                                      final Consumer<String> target) {
        if (source.get().length() > 192) {
            final Text label = new Text.Builder(Button.BIG_WIDTH, Button.DEFAULT_HEIGHT,
                Component.literal(source.get().substring(0, 128)), font).build();
            label.setTooltip(Tooltip.create(LONG_STRING));
            return new Element(getTranslation(key), getTooltipTranslation(key, null), label);
        }
        final Input box = new Input.Builder(font, 0, 0, Button.BIG_WIDTH, Button.DEFAULT_HEIGHT,
            getTranslation(key)).editable(true).maxLength(Math.clamp(source.get().length() + 5, 128, 192))
            .value(source.get()).build();
        box.setResponder(newValue -> {
            if (newValue == null || !tester.test(newValue)) {
                box.setTextColor(0xffff0000);
                return;
            }
            box.setTextColor(Input.DEFAULT_TEXT_COLOR);
            if (newValue.equals(source.get())) return;
            undoManager.add(new UndoAction<>(target, key, this::onChanged), newValue, source.get());
        });

        return new Element(getTranslation(key), getTooltipTranslation(key, null), box);
    }

    private Element createBooleanValue(final String key, final Supplier<Boolean> source,
                                       final Consumer<Boolean> target) {
        final Toggle toggle = new Toggle.Builder().checked(source.get()).onChange(
            (ignored, newValue) -> undoManager.add(new UndoAction<>(target, key, this::onChanged), newValue,
                source.get())).build();

        return new Element(getTranslation(key), getTooltipTranslation(key, null), toggle);
    }

    private <T extends Enum<T>> Element createEnumValue(final String key, final ModConfigSpec.ValueSpec spec,
                                                        final Supplier<T> source, final Consumer<T> target) {
        @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>) spec.getClazz();
        assert clazz != null;
        final List<T> list = Arrays.stream(clazz.getEnumConstants()).filter(spec::test).toList();
        final GuiDropdown<T> dropdown = new GuiDropdown<>(source.get(),
            displayValue -> displayValue instanceof TranslatableEnum translatableEnum ?
                translatableEnum.getTranslatedName() : Component.literal(
                displayValue.name()), (ignored, newValue) -> {
            if (source.get().equals(newValue)) return;
            undoManager.add(new UndoAction<>(target, key, this::onChanged), newValue, source.get());
        }, list);

        return new Element(getTranslation(key), getTooltipTranslation(key, null), dropdown);
    }

    private Element createSection(final String key, final UnmodifiableConfig subconfig,
                                  final UnmodifiableConfig subsection) {
        if (subconfig.isEmpty()) return null;

        Button button = Button.Builder.normal(Component.translatable(SECTION, Component.translatable(
            TranslationChecker.getWithFallback(getTranslationKey(key) + ".button", SECTION_TEXT)))).onPress(ignored -> {
            if (minecraft == null) return;
            //noinspection deprecation
            minecraft.setScreen(sectionCache.computeIfAbsent(key, k -> new ConfigurationSectionScreen(
                ConfigContext.section(this.context, this, subsection.entrySet(), subconfig.valueMap(), key),
                Component.translatable(CRUMB, this.getTitle(), CRUMB_SEPARATOR, getTranslation(key)))));
        }).build();

        return new Element(Component.translatable(SECTION, getTranslation(key)), getTooltipTranslation(key, null),
            button);
    }

    private Element createOtherValue(final String key, final ModConfigSpec.ConfigValue<?> value) {
        final Text label = new Text.Builder(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT,
            Component.literal(Objects.toString(value.getRaw())), font).build();
        label.setTooltip(Tooltip.create(UNSUPPORTED_ELEMENT));
        return new Element(getTranslation(key), getTooltipTranslation(key, null), label);
    }

    private Element createIntegerValue(final String key, final ModConfigSpec.ValueSpec spec,
                                       final Supplier<Integer> source, final Consumer<Integer> target) {
        final @Nullable ModConfigSpec.Range<Integer> range = spec.getRange();
        if (range == null) return createNumberBox(key, spec, source, target, Integer::decode, 0);

        if ((long) range.getMax() - (long) range.getMin() < MAX_SLIDER_SIZE)
            return createSlider(key, source, target, range);
        else return createNumberBox(key, spec, source, target, Integer::decode, 0);
    }

    private Element createSlider(final String key, final Supplier<Integer> source, final Consumer<Integer> target,
                                 ModConfigSpec.Range<Integer> range) {
        final Slider slider = new Slider(range.getMin(), range.getMax(), source.get());
        slider.onChange((ignore, newValue) -> {
            if (newValue == source.get()) return;
            undoManager.add(new UndoAction<>(target, key, this::onChanged), newValue, source.get());
        });

        return new Element(getTranslation(key), getTooltipTranslation(key, range), slider);
    }

    private Element createLongValue(final String key, final ModConfigSpec.ValueSpec spec, final Supplier<Long> source,
                                    final Consumer<Long> target) {
        return createNumberBox(key, spec, source, target, Long::decode, 0L);
    }

    private Element createDoubleValue(final String key, final ModConfigSpec.ValueSpec spec,
                                      final Supplier<Double> source, final Consumer<Double> target) {
        return createNumberBox(key, spec, source, target, Double::parseDouble, 0.0);
    }

    private <T extends Number & Comparable<? super T>> Element createNumberBox(final String key,
                                                                               final ModConfigSpec.ValueSpec spec,
                                                                               final Supplier<T> source,
                                                                               final Consumer<T> target,
                                                                               final Function<String, T> parser,
                                                                               final T zero) {
        ModConfigSpec.Range<T> range = spec.getRange();
        final Input box = new Input.Builder(font, 0, 0, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT,
            getTranslation(key)).editable(true).filter(str -> {
            try {
                parser.apply(str);
                return true;
            } catch (final NumberFormatException e) {
                return isPartialNumber(str, (range == null || range.getMin().compareTo(zero) < 0));
            }
        }).value(source.get() + "").build();
        box.setResponder(str -> {
            try {
                final T newValue = parser.apply(str);
                if (newValue != null && (range == null || range.test(newValue)) && spec.test(newValue)) {
                    box.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
                    if (!newValue.equals(source.get()))
                        undoManager.add(new UndoAction<>(target, key, this::onChanged), newValue, source.get());
                    return;
                }
            } catch (final NumberFormatException e) {
                // field probably is just empty/partial, ignore that
            }
            box.setTextColor(0xFFFF0000);
        });
        return new Element(getTranslation(key), getTooltipTranslation(key, range), box);
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

    @Override
    public void onClose() {
        if (changed) {
            if (parent instanceof ConfigurationSectionScreen screen) {
                // "bubble up" the marker so the top-most section can change the ModConfig
                screen.changed = true;
            } else {
                // we are a top-level per-type config screen, i.e. one specific config file. Save the config and tell
                // the mod to reload.
                context.modSpec().save();
            }

            // the restart flag only matters when there were actual changes
            if (parent instanceof final ConfigurationSectionScreen screen) {
                screen.needsRestart = screen.needsRestart.with(needsRestart);
            } else if (parent instanceof final ConfigurationSelectionScreen screen) {
                screen.needsRestart = screen.needsRestart.with(needsRestart);
            }
        }
        super.onClose();
    }

    public record Element(@Nullable Component name, @Nullable Component tooltip, AbstractWidget widget) {
    }

    private record UndoAction<T>(Consumer<T> target, String key,
                                 Consumer<String> onChange) implements Consumer<T> {
        @Override
        public void accept(T v) {
            target.accept(v);
            onChange.accept(key);
        }
    }
}
