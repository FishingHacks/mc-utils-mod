package net.fishinghacks.utils.modules;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.config.values.AbstractCachedValue;
import net.fishinghacks.utils.config.values.ModuleToggle;
import net.fishinghacks.utils.platform.Services;

import java.util.HashMap;
import java.util.HashSet;

public class ModuleManager implements ModuleManagerLike {
    public static final HashMap<String, IModule> modules = new HashMap<>();
    public static final HashSet<String> enabledModules = new HashSet<>();

    public static final ModuleManager instance = new ModuleManager();


    static {
        Services.PLATFORM.scanForAnnotation(Module.class).forEach(clazz -> {
            try {
                addModule((IModule) clazz.getConstructor().newInstance());
            } catch (Exception e) {
                Constants.LOG.info("Failed to construct a {}", clazz.getName(), e);
            }
        });

        ModuleToggle.manager = instance;
        ModuleToggle.toggles.values().forEach(AbstractCachedValue::clearCache);
    }

    public static void addModule(IModule... modules) {
        for (var module : modules) {
            ModuleManager.modules.put(module.name(), module);
            module.enabled = false;
            module.onDisable();
        }
    }

    private static void saveModules() {
        Configs.clientConfig.ENABLED_MODULES.set(enabledModules.stream().toList());
        ModuleToggle.toggles.values().forEach(AbstractCachedValue::clearCache);
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

    @Override
    public boolean isEnabled(String module) {
        return enabledModules.contains(module);
    }

    @Override
    public void setEnabled(String module, boolean value) {
        if (enabledModules.contains(module) == value) return;
        if (value) enabledModules.add(module);
        else enabledModules.remove(module);
        modules.get(module).setEnabled(value);
        saveModules();
    }
}
