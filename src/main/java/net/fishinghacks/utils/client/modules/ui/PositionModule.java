package net.fishinghacks.utils.client.modules.ui;

import net.fishinghacks.utils.client.modules.ModuleCategory;
import net.fishinghacks.utils.client.modules.RenderableTextModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PositionModule extends RenderableTextModule {

    @Override
    public List<Component> getText() {
        assert Minecraft.getInstance().player != null;
        Vec3 pos = Minecraft.getInstance().player.position();
        String posText = (int)pos.x + " " + (int)pos.y + " " + (int)pos.z;
        return List.of(Component.literal("XYZ: ").withStyle(ChatFormatting.GRAY).append(Component.literal(posText).withStyle(ChatFormatting.AQUA)));
    }

    @Override
    public List<Component> getPreviewText() {
        return List.of(Component.literal("XYZ: ").withStyle(ChatFormatting.GRAY).append(Component.literal("0 0 0").withStyle(ChatFormatting.AQUA)));
    }

    @Override
    public String name() {
        return "position";
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.UI;
    }
}
