package net.fishinghacks.utils.gui.mcsettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.layouts.LinearLayout;

import java.util.List;

public class Chat implements OptionSubscreen {
    private static List<OptionInstance<?>> options(Options options) {
        return List.of(options.chatVisibility(), options.chatColors(), options.chatLinks(), options.chatLinksPrompt(),
            options.chatOpacity(), options.textBackgroundOpacity(), options.chatScale(), options.chatLineSpacing(),
            options.chatDelay(), options.chatWidth(), options.chatHeightFocused(), options.chatHeightUnfocused(),
            options.narrator(), options.autoSuggestions(), options.hideMatchedNames(), options.reducedDebugInfo(),
            options.onlyShowSecureChat());
    }

    @Override
    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        OptionInstance<?> narrator = options.narrator();
        boolean narratorAvailable = Minecraft.getInstance().getNarrator().isActive();

        for(var instance : options(options)) {
            var widget = layout.addChild(OptionUtils.configFromOptionInstance(instance, configWidth));
            if(instance == narrator) widget.getWidget().active = narratorAvailable;
        }
    }
}
