package net.fishinghacks.utils.client.modules;

import net.fishinghacks.utils.client.modules.misc.Freecam;
import net.fishinghacks.utils.client.modules.misc.Freezecam;
import net.fishinghacks.utils.client.modules.misc.Fullbright;
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
        addModule(new AllowBlockedKeys(), new Fullbright(), new Freezecam(), new Freecam());
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

    public static void enableModule(String name) {
        if(enabledModules.contains(name)) return;
        enabledModules.add(name);
        modules.get(name).setEnabled(true);
        saveModules();
    }

    public static void disableModule(String name) {
        if(!enabledModules.contains(name)) return;
        enabledModules.remove(name);
        modules.get(name).setEnabled(false);
        saveModules();
    }

    public static void toggleModule(String name) {
        if(enabledModules.contains(name)) enabledModules.remove(name);
        else enabledModules.add(name);
        modules.get(name).toggle();
        saveModules();
    }
}
