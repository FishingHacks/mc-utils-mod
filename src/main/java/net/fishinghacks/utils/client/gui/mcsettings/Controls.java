package net.fishinghacks.utils.client.gui.mcsettings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.layouts.LinearLayout;

import java.util.List;

public class Controls implements OptionSubscreen {
    public static List<OptionInstance<?>> options(Options options) {
        return List.of(options.toggleCrouch(), options.toggleSprint(), options.autoJump(), options.operatorItemsTab(), options.sensitivity(), options.invertYMouse(), options.mouseWheelSensitivity(), options.discreteMouseScroll(), options.touchscreen());
    }

    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        for(OptionInstance<?> instance : options(options))
            layout.addChild(OptionUtils.configFromOptionInstance(instance, configWidth));
        if(InputConstants.isRawMouseInputSupported())
            layout.addChild(OptionUtils.configFromOptionInstance(options.rawMouseInput(), configWidth));
    }
}
