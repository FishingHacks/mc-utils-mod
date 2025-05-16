package net.fishinghacks.utils.modules;

import net.fishinghacks.utils.modules.misc.*;
import net.fishinghacks.utils.modules.ui.*;

import java.util.HashMap;
import java.util.HashSet;

public class ModuleManager {
    public static final HashMap<String, Module> modules = new HashMap<>();
    public static final HashSet<String> enabledModules = new HashSet<>();

    static {
        // -- UI --
        addModule(new PositionModule(), new FpsModule(), new PingModule(), new PositionModule(), new ModListModule(),
            new KeystrokeMod(), new ClockModule(), new ServerDisplayModule());

        // -- MISC --
        addModule(new AllowBlockedKeys(), new Fullbright(), new Freezecam(), new Freecam(), new Zoom(), new Tablist());
    }

    public static void addModule(Module... modules) {
        for (var module : modules) {
            ModuleManager.modules.put(module.name(), module);
            module.enabled = false;
            module.onDisable();
        }
    }

    private static void saveModules() {
        //Configs.clientConfig.ENABLED_MODULES.set(enabledModules.stream().toList());
    }

    public static void enableModule(String name) {
        if (enabledModules.contains(name)) return;
        enabledModules.add(name);
        modules.get(name).setEnabled(true);
        saveModules();
    }

    public static void disableModule(String name) {
        if (!enabledModules.contains(name)) return;
        enabledModules.remove(name);
        modules.get(name).setEnabled(false);
        saveModules();
    }

    public static void toggleModule(String name) {
        if (enabledModules.contains(name)) enabledModules.remove(name);
        else enabledModules.add(name);
        modules.get(name).toggle();
        saveModules();
    }
}
