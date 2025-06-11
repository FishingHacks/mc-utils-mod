package net.fishinghacks.utils.gui.mcsettings;

import net.minecraft.client.Options;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.sounds.SoundSource;

class Sounds implements OptionSubscreen {
    @Override
    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        for(SoundSource soundSource : SoundSource.values()) {
            var opt = options.getSoundSourceOptionInstance(soundSource);
            layout.addChild(OptionUtils.configFromOptionInstance(opt, configWidth));
        }
        layout.addChild(OptionUtils.configFromOptionInstance(options.soundDevice(), configWidth));
        layout.addChild(OptionUtils.configFromOptionInstance(options.showSubtitles(), configWidth));
        layout.addChild(OptionUtils.configFromOptionInstance(options.directionalAudio(), configWidth));
    }
}
