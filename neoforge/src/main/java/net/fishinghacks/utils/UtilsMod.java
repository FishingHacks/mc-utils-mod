package net.fishinghacks.utils;

import net.fishinghacks.utils.config.ConfigsImpl;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class UtilsMod {
    public static ModContainer container;

    public UtilsMod(ModContainer modContainer) {
        container = modContainer;
        ConfigsImpl.register(modContainer);
    }
}