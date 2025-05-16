package net.fishinghacks.utils;

import net.fabricmc.api.ModInitializer;
import net.fishinghacks.utils.config.Configs;

public class UtilsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Configs.register(null, (ignored0, ignored1) -> {
        });
    }
}
