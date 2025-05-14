package net.fishinghacks.utils.client.modules.misc;

import net.fishinghacks.utils.client.modules.Module;
import net.fishinghacks.utils.client.modules.ModuleCategory;
import net.neoforged.neoforge.client.event.InputEvent;

public class Zoom extends Module {
    public static boolean isEnabled;
    public static float fov;

    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if(!isEnabled) return;
        event.setCanceled(true);
        fov -= (float)event.getScrollDeltaY();
        fov = Math.clamp(fov, 0.1f, 110f);
    }

    @Override
    public void onToggle() {
        Zoom.isEnabled = super.enabled;
        fov = 30f;
    }

    @Override
    public String name() {
        return "zoom";
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.MISC;
    }
}
