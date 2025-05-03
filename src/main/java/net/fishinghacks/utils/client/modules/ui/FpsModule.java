package net.fishinghacks.utils.client.modules.ui;

import net.fishinghacks.utils.common.Translation;
import net.fishinghacks.utils.common.config.CachedValue;
import net.fishinghacks.utils.common.config.Config;
import net.fishinghacks.utils.client.modules.ModuleCategory;
import net.fishinghacks.utils.client.modules.RenderableTextModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class FpsModule extends RenderableTextModule {
    static CachedValue<Boolean> colored;

    @Override
    public void buildConfig(Config cfg, ModConfigSpec.Builder builder) {
        colored = CachedValue.wrap(cfg, Translation.FpsConfigColored.config(builder).define("colored", true));
        super.buildConfig(cfg, builder);
    }

    @Override
    public List<Component> getText() {
        assert Minecraft.getInstance().player != null;
        int fps = Minecraft.getInstance().getFps();
        MutableComponent comp = Component.literal(""+fps);
        if (colored.get()) {
            if(fps > 30) comp = comp.withStyle(ChatFormatting.GREEN);
            else if(fps > 15) comp = comp.withStyle(ChatFormatting.YELLOW);
            else comp = comp.withStyle(ChatFormatting.RED);
        }
        return List.of(Component.literal("FPS: ").withStyle(ChatFormatting.GRAY).append(comp));
    }

    @Override
    public List<Component> getPreviewText() {
        return List.of(Component.literal("FPS: ").withStyle(ChatFormatting.GRAY).append(Component.literal("0").withStyle(ChatFormatting.GREEN)));
    }

    @Override
    public String name() {
        return "fps";
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.UI;
    }
}
