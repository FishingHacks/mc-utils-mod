package net.fishinghacks.utils;


import net.fishinghacks.utils.config.ConfigsImpl;
import net.fishinghacks.utils.gui.configuration.ConfigurationSelectionScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Constants.MOD_ID)
public class UtilsMod {
    public static ModContainer container;

    public UtilsMod(ModContainer modContainer) {
        container = modContainer;
        CommonClass.init();
        ConfigsImpl.register(modContainer);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationSelectionScreen::new);
    }
}