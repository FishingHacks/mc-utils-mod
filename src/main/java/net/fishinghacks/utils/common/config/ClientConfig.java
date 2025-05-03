package net.fishinghacks.utils.common.config;

import net.fishinghacks.utils.common.Translation;
import net.fishinghacks.utils.client.modules.ModuleManager;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientConfig extends Config {
    private final ModConfigSpec configSpec;

    public final CachedValue<String> CMD_PREFIX;
    public final CachedValue<Boolean> CUSTOM_MENUS;
    public final CachedValue<Boolean> SHOW_SPLASH;
    public final CachedValue<Boolean> SHOW_PANORAMA;
    public final CachedValue<List<? extends String>> ENABLED_MODULES;
    public final CachedValue<List<? extends String>> SERVICE_SERVER_HISTORY;
    public final CachedValue<Boolean> AUTOCONNECT;

    ClientConfig() {
        var builder = new ModConfigSpec.Builder();

        CMD_PREFIX = CachedValue.wrap(this, Translation.GuiConfigCmdPrefix.config(builder)
            .define("cmd_prefix", ".", s -> s instanceof String str && str.length() == 1));

        CUSTOM_MENUS = CachedValue.wrap(this, builder.define("custom_menus", false));
        SHOW_SPLASH = CachedValue.wrap(this, builder.define("show_splash", true));
        SHOW_PANORAMA = CachedValue.wrap(this, builder.define("show_panorama", true));

        ENABLED_MODULES = CachedValue.wrap(this,
            builder.defineListAllowEmpty("_active_mods", List.of(), () -> "", v -> v instanceof String));
        ENABLED_MODULES.onInvalidate(() -> {
            ModuleManager.enabledModules.clear();
            for (var v : ENABLED_MODULES.get())
                if (v instanceof String s) {
                    var mod = ModuleManager.modules.get(s);
                    if (mod == null) continue;
                    ModuleManager.enabledModules.add(s);
                    mod.setEnabled(true);
                }

        });
        SERVICE_SERVER_HISTORY = CachedValue.wrap(this,
            builder.defineListAllowEmpty("_service_server_history", List.of(), () -> "", v -> v instanceof String));
        AUTOCONNECT = CachedValue.wrap(this, builder.define("autoconnect", false));

        for (var entry : ModuleManager.modules.values()) {
            builder.push(entry.name());
            entry.buildConfig(this, builder);
            builder.pop();
        }

        configSpec = builder.build();
    }

    @Override
    public @NotNull ModConfigSpec getModConfigSpec() {
        return configSpec;
    }

    @Override
    public ModConfig.Type getConfigType() {
        return ModConfig.Type.CLIENT;
    }
}
