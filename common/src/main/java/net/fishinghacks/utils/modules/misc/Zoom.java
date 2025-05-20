package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;

@Module(name = "zoom", category = ModuleCategory.MISC)
public class Zoom extends IModule {
    public static boolean isEnabled;
    public static float fov;

    public static boolean onScroll(float scrollDeltaY) {
        if(!isEnabled) return false;
        fov -= scrollDeltaY;
        fov = Math.clamp(fov, 0.1f, 110f);
        return true;
    }

    @Override
    public void onToggle() {
        Zoom.isEnabled = super.enabled;
        fov = 30f;
    }
}
