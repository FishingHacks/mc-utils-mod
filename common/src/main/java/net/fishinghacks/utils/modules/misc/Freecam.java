package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.ModuleManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

@Module(name = "freecam", category = ModuleCategory.MISC)
public class Freecam extends IModule {
    public static boolean isEnabled;

    @Override
    public void onToggle() {
        Freecam.isEnabled = super.enabled;
    }

    @Override
    public void onEnable() {
        ModuleManager.disableModule("freezecam");
        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }
}
