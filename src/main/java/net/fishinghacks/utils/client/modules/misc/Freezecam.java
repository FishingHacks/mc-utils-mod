package net.fishinghacks.utils.client.modules.misc;

import net.fishinghacks.utils.client.modules.Module;
import net.fishinghacks.utils.client.modules.ModuleCategory;
import net.fishinghacks.utils.client.modules.ModuleManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class Freezecam extends Module {
    public static boolean isEnabled;

    @Override
    public void onToggle() {
        Freezecam.isEnabled = super.enabled;
    }

    @Override
    public void onEnable() {
        ModuleManager.disableModule("freecam");
        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    @Override
    public String name() {
        return "freezecam";
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.MISC;
    }
}
