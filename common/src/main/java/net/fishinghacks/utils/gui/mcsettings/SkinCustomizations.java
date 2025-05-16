package net.fishinghacks.utils.gui.mcsettings;

import net.fishinghacks.utils.gui.components.GuiDropdown;
import net.fishinghacks.utils.gui.components.Toggle;
import net.fishinghacks.utils.gui.components.ConfigSection;
import net.minecraft.client.Options;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

public class SkinCustomizations implements OptionSubscreen {
    @Override
    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        for (PlayerModelPart part : PlayerModelPart.values()) {
            Toggle widget = new Toggle.Builder(part.getName()).onChange((ignored, v) -> options.setModelPart(part, v))
                .checked(options.isModelPartEnabled(part)).build();
            layout.addChild(new ConfigSection(part.getName(), widget, configWidth));
        }

        GuiDropdown<HumanoidArm> mainhandDropdown = new GuiDropdown<>(options.mainHand().get(),
            v -> Component.translatable(v.getKey()), HumanoidArm.values());
        mainhandDropdown.onValueChange((ignored, newValue) -> options.mainHand().set(newValue));
        layout.addChild(new ConfigSection(Component.translatable("options.mainHand"), mainhandDropdown, configWidth));
    }
}
