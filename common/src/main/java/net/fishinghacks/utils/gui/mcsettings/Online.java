package net.fishinghacks.utils.gui.mcsettings;

import net.minecraft.client.Options;
import net.minecraft.client.gui.layouts.LinearLayout;

public class Online implements OptionSubscreen {
    @Override
    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        layout.addChild(OptionUtils.configFromOptionInstance(options.realmsNotifications(), configWidth));
        layout.addChild(OptionUtils.configFromOptionInstance(options.allowServerListing(), configWidth));
    }
}
