package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.RenderableTextModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

@Module(name = "fps", category = ModuleCategory.UI)
public class Fps extends RenderableTextModule {
    static CachedValue<Boolean> colored;

    @Override
    public void buildConfig(AbstractConfig cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);
        colored = CachedValue.wrap(cfg, builder, "colored", true);
    }

    @Override
    public List<Component> getText() {
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
}
