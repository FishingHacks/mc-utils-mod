package net.fishinghacks.utils.client.modules.ui;

import net.fishinghacks.utils.client.gui.MainScreen;
import net.fishinghacks.utils.client.modules.ModuleCategory;
import net.fishinghacks.utils.client.modules.RenderableTextModule;
import net.fishinghacks.utils.common.Translation;
import net.fishinghacks.utils.common.config.CachedValue;
import net.fishinghacks.utils.common.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerDisplayModule extends RenderableTextModule {
    static CachedValue<Boolean> colored;

    @Override
    public void buildConfig(Config cfg, ModConfigSpec.Builder builder) {
        colored = CachedValue.wrap(cfg, Translation.FpsConfigColored.config(builder).define("colored", true));
        super.buildConfig(cfg, builder);
    }

    @Override
    public List<Component> getText() {
        assert Minecraft.getInstance().player != null;
        var serverIp = Minecraft.getInstance().getCurrentServer() != null ? Component.literal(
                Minecraft.getInstance().getCurrentServer().ip)
            .withStyle(ChatFormatting.AQUA) : MainScreen.SINGLEPLAYER.copy().withStyle(ChatFormatting.LIGHT_PURPLE);
        return List.of(Translation.ServerDisplayPrefix.with().withStyle(ChatFormatting.GRAY).append(serverIp));
    }

    @Override
    public List<Component> getPreviewText() {
        return List.of(Translation.ServerDisplayPrefix.with().withStyle(ChatFormatting.GRAY)
            .append(Component.literal("example.com").withStyle(ChatFormatting.AQUA)));
    }

    @Override
    public String name() {
        return "server_display";
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.UI;
    }
}
