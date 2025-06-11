package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.ModuleManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

@Module(name = "freezecam", category = ModuleCategory.MISC)
public class Freezecam extends IModule {
    public static Freezecam instance;

    public Freezecam() {
        instance = this;
    }

    @Override
    public void onEnable() {
        ModuleManager.disableModule("freecam");
        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }
}
