package net.fishinghacks.utils.config.values;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.mojang.blaze3d.platform.InputConstants;
import net.fishinghacks.utils.actions.Action;
import net.fishinghacks.utils.actions.ActionType;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.platform.services.ConfigValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ActionListCachedValue extends CachedMappedValue<List<Action>, List<? extends Config>> {

    protected ActionListCachedValue(net.fishinghacks.utils.config.spec.Config config,
                                    ConfigValue<List<? extends Config>> internalValue,
                                    Function<List<Action>, List<? extends Config>> encode,
                                    Function<List<? extends Config>, List<Action>> decode, ConfigBuilder builder) {
        super(config, internalValue, encode, decode, builder);
    }

    protected static List<Action> fromConfig(List<? extends Config> cfgs) {
        var list = new ArrayList<Action>();
        int id = 0;
        for (var config : cfgs) {
            var cfgType = config.getRaw("type");
            var cfgKey = config.getRaw("key");
            var cfgValue = config.getRaw("value");
            ActionType actualType = null;
            InputConstants.Key key = InputConstants.UNKNOWN;
            if (cfgType instanceof String) for (var v : ActionType.values())
                if (v.name().equals(cfgType)) {
                    actualType = v;
                    break;
                }
            if (cfgKey instanceof String keyString) try {
                key = InputConstants.getKey(keyString);
            } catch (Exception ignored) {
            }
            if (actualType == null) continue;
            Action action = actualType.create();
            action.setKey(key);
            action.setId(id++);
            if (cfgValue instanceof String s) {
                var validValues = action.validValues();
                if (validValues == null || validValues.contains(s)) action.setValue(s);
            }
            list.add(action);
        }
        return list;
    }

    protected static List<? extends Config> toConfig(List<Action> actions) {
        var list = new ArrayList<Config>();
        for (var action : actions) {
            var cfg = InMemoryFormat.withUniversalSupport().createConfig();
            cfg.set("type", action.type().name());
            cfg.set("key", action.key().getName());
            cfg.set("value", action.getValue());
            list.add(cfg);
        }
        return list;
    }

    public static ActionListCachedValue wrap(net.fishinghacks.utils.config.spec.Config config, ConfigBuilder builder,
                                             String key) {
        return new ActionListCachedValue(config,
            builder.inner().defineListAllowEmpty(key, List.of(), () -> null, v -> v instanceof Config),
            ActionListCachedValue::toConfig, ActionListCachedValue::fromConfig, builder);
    }
}
