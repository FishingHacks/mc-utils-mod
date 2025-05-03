package net.fishinghacks.utils.client.modules;

import net.fishinghacks.utils.common.config.Configs;
import net.fishinghacks.utils.client.modules.misc.AllowBlockedKeys;
import net.fishinghacks.utils.client.modules.ui.FpsModule;
import net.fishinghacks.utils.client.modules.ui.ModListModule;
import net.fishinghacks.utils.client.modules.ui.PingModule;
import net.fishinghacks.utils.client.modules.ui.PositionModule;

import java.util.HashMap;
import java.util.HashSet;

public class ModuleManager {
    public static final HashMap<String, Module> modules = new HashMap<>();
    public static final HashSet<String> enabledModules = new HashSet<>();

    static {
        // -- UI --
        addModule(new PositionModule(), new FpsModule(), new PingModule(), new PositionModule(), new ModListModule());
        // -- MISC --
        addModule(new AllowBlockedKeys());
    }

    public static void addModule(Module... modules) {
        for (var module : modules) {
            ModuleManager.modules.put(module.name(), module);
            module.enabled = false;
            module.onDisable();
        }
    }

    private static void saveModules() {
        Configs.clientConfig.ENABLED_MODULES.set(enabledModules.stream().toList());
    }

    public static void toggleModule(String name) {
        if(enabledModules.contains(name)) enabledModules.remove(name);
        else enabledModules.add(name);
        modules.get(name).toggle();
        saveModules();
    }
}
