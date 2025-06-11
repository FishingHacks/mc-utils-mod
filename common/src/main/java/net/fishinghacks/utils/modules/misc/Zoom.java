package net.fishinghacks.utils.modules.misc;

import net.fishinghacks.utils.modules.IModule;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;

@Module(name = "zoom", category = ModuleCategory.MISC)
public class Zoom extends IModule {
    public static Zoom instance;
    public static float fov = 30f;

    public Zoom() {
        instance = this;
    }

    public static boolean onScroll(float scrollDeltaY) {
        if(!instance.isEnabled()) return false;
        fov -= scrollDeltaY;
        fov = Math.clamp(fov, 0.1f, 110f);
        return true;
    }

    @Override
    public void onToggle() {
        fov = 30f;
    }
}
