package net.fishinghacks.utils.gui.mcsettings;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.gui.components.GuiDropdown;
import net.fishinghacks.utils.gui.components.Slider;
import net.fishinghacks.utils.gui.components.Toggle;
import net.fishinghacks.utils.gui.components.ConfigSection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

public class OptionUtils {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ConfigSection configFromOptionInstance(OptionInstance instance, int configWidth) {
        AbstractWidget widget;
        if (instance.get().getClass() == Boolean.class) {
            widget = new Toggle.Builder().checked((Boolean) instance.get()).onChange((ignored, v) -> instance.set(v))
                .build();
        } else if (instance.values() instanceof OptionInstance.Enum enumSet) {
            widget = new GuiDropdown<>(instance.caption, instance.get(), instance.toString,
                (ignored, value) -> instance.set(value), enumSet.values());
            widget.setWidth(Slider.DEFAULT_WIDTH);
        } else if (instance.values() instanceof OptionInstance.LazyEnum enumSet) {
            widget = new GuiDropdown<>(instance.caption, instance.get(), instance.toString,
                (ignored, value) -> instance.set(value), (List) enumSet.values().get());
            widget.setWidth(Slider.DEFAULT_WIDTH);
        } else if (instance.values() instanceof OptionInstance.AltEnum enumSet) {
            widget = new GuiDropdown<>(instance.caption, instance.get(), instance.toString,
                (ignored, value) -> instance.set(value), enumSet.values());
            widget.setWidth(Slider.DEFAULT_WIDTH);
        } else if (instance.values() instanceof OptionInstance.UnitDouble unitDouble) {
            widget = fromSliderValueSet(instance, unitDouble, unitDouble::fromSliderValue, unitDouble::toSliderValue);
        } else if (instance.values() instanceof OptionInstance.IntRange range) {
            widget = fromIntRage(instance, range.minInclusive(), range.maxInclusive());
        } else if (instance.values() instanceof OptionInstance.ClampingLazyMaxIntRange range) {
            widget = fromIntRage(instance, range.minInclusive(), range.maxInclusive());
        } else widget = fromValueSet(instance);
        if (widget == null)
            widget = new StringWidget(Component.literal("No widget found"), Minecraft.getInstance().font);


        return new ConfigSection(instance.caption, getTooltip(instance), widget, configWidth);
    }

    public static <T> @Nullable Component getTooltip(OptionInstance<T> instance) {
        try {
            var tooltipField = OptionInstance.class.getDeclaredField("tooltip");
            tooltipField.setAccessible(true);
            @SuppressWarnings("unchecked") var supplier = (OptionInstance.TooltipSupplier<T>) tooltipField.get(
                instance);
            var tooltip = supplier.apply(instance.get());
            tooltipField.setAccessible(false);

            var messageField = Tooltip.class.getDeclaredField("message");
            messageField.setAccessible(true);
            Component message = (Component) messageField.get(tooltip);
            messageField.setAccessible(false);
            return message;
        } catch (Exception e) {
            return null;
        }
    }

    private static AbstractWidget fromIntRage(OptionInstance<Integer> instance, int min, int max) {
        Slider widget = new Slider(min, max, instance.get(), instance.toString.apply(instance.get()));
        widget.onChange((s, newValue) -> {
            var v = instance.values().validateValue(newValue);
            if (v.isPresent()) {
                instance.set(v.get());
                s.setMessage(instance.toString.apply(v.get()));
            } else s.setMessage(instance.toString.apply(newValue));
        });
        return widget;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static @Nullable AbstractWidget fromValueSet(OptionInstance inst) {
        OptionInstance.ValueSet set = inst.values();
        try {
            Method toSliderValue = null;
            Method fromSliderValue = null;

            for (Method method : set.getClass().getMethods()) {
                if (method.getName().equals("toSliderValue") && method.getParameterCount() == 1) {
                    if (toSliderValue != null) throw new Exception("multiple methods named toSliderValue");
                    toSliderValue = method;
                }
                if (method.getName().equals("fromSliderValue") && method.getParameterCount() == 1) {
                    if (fromSliderValue != null) throw new Exception("multiple methods named fromSliderValue");
                    fromSliderValue = method;
                }
            }

            if (toSliderValue == null || fromSliderValue == null) {
                Constants.LOG.info("Not enough methods found for {}", inst.caption);
                return null;
            }

            toSliderValue.setAccessible(true);
            fromSliderValue.setAccessible(true);

            final Method finalFromSliderValue = fromSliderValue;
            final Method finalToSliderValue = toSliderValue;
            return fromSliderValueSet(inst, set, num -> {
                try {
                    return finalFromSliderValue.invoke(set, num);
                } catch (Exception e) {
                    return null;
                }
            }, num -> {
                try {
                    return (Double) finalToSliderValue.invoke(set, num);
                } catch (Exception e) {
                    return null;
                }
            });
        } catch (Exception e) {
            Constants.LOG.info("Error for {}", inst.caption, e);
            return null;
        }
    }

    private static <T, S extends OptionInstance.ValueSet<T>> AbstractWidget fromSliderValueSet(OptionInstance<T> inst,
                                                                                               S set,
                                                                                               Function<Double, T> fromSliderValue,
                                                                                               Function<T, Double> toSliderValue) {
        var widget = new Slider(0, 256, (int) (toSliderValue.apply(inst.get()) * 256.0),
            inst.toString.apply(inst.get()));
        widget.onChange((slider, value) -> {
            try {
                double doubleValue = Math.clamp((double) value / 256.0, 0.0, 1.0);
                var v = fromSliderValue.apply(doubleValue);
                var val = set.validateValue(v);
                if (val.isPresent()) {
                    inst.set(val.get());
                    Component comp = inst.toString.apply(val.get());
                    if (comp != null) slider.setMessage(comp);
                } else {
                    Component comp = inst.toString.apply(v);
                    if (comp != null) slider.setMessage(comp);
                }
            } catch (Exception e) {
                MutableComponent err = Component.literal("Error: " + e.getMessage());
                for (var stack : e.getStackTrace())
                    err.append(stack.toString());

                slider.setMessage(err.withStyle(ChatFormatting.RED));
            }
        });
        return widget;
    }
}
