package net.fishinghacks.utils;

import net.fishinghacks.utils.gui.configuration.ConfigSectionScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class UtilsClientMod {
    public UtilsClientMod(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
            (ignored, parent) -> new ConfigSectionScreen(parent));
    }
}