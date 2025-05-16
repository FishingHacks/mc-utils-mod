package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.config.CachedValue;
import net.fishinghacks.utils.config.Config;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.RenderableTextModule;
import net.fishinghacks.utils.platform.services.IConfigBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class FpsModule extends RenderableTextModule {
    static CachedValue<Boolean> colored;

    @Override
    public void buildConfig(Config cfg, IConfigBuilder builder) {
        super.buildConfig(cfg, builder);
        colored = CachedValue.wrap(cfg, Translation.FpsConfigColored.config(builder).define("colored", true));
    }

    @Override
    public List<Component> getText() {
        assert Minecraft.getInstance().player != null;
        int fps = Minecraft.getInstance().getFps();
        MutableComponent comp = Component.literal("" + fps);
        if (colored.get()) {
            if (fps > 30) comp = comp.withStyle(ChatFormatting.GREEN);
            else if (fps > 15) comp = comp.withStyle(ChatFormatting.YELLOW);
            else comp = comp.withStyle(ChatFormatting.RED);
        }
        return List.of(Component.literal("FPS: ").withStyle(ChatFormatting.GRAY).append(comp));
    }

    @Override
    public List<Component> getPreviewText() {
        return List.of(Component.literal("FPS: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("0").withStyle(ChatFormatting.GREEN)));
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
