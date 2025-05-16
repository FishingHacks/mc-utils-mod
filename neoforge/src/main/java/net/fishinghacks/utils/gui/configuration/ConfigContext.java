package net.fishinghacks.utils.gui.configuration;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ConfigContext(String modId, Screen parent, ModConfigSpec modSpec,
                            Set<? extends UnmodifiableConfig.Entry> entries, Map<String, Object> valueSpecs,
                            List<String> keylist) {

    public static ConfigContext top(String modId, Screen parent, ModConfig modConfig) {
        //noinspection deprecation
        return new ConfigContext(modId, parent, (ModConfigSpec) modConfig.getSpec(), ((ModConfigSpec) modConfig.getSpec()).getValues().entrySet(), ((ModConfigSpec) modConfig.getSpec()).getSpec().valueMap(), List.of());
    }

    public static ConfigContext section(ConfigContext parentContext, Screen parent, Set<? extends UnmodifiableConfig.Entry> entries, Map<String, Object> valueSpecs, String key) {
        return new ConfigContext(parentContext.modId, parent, parentContext.modSpec, entries, valueSpecs, parentContext.makeKeyList(key));
    }

    public static ConfigContext list(ConfigContext parentContext, Screen parent) {
        return new ConfigContext(parentContext.modId, parent, parentContext.modSpec, parentContext.entries, parentContext.valueSpecs, parentContext.keylist);
    }

    ArrayList<String> makeKeyList(String key) {
        ArrayList<String> result = new ArrayList<>(this.keylist);
        result.add(key);
        return result;
    }
}