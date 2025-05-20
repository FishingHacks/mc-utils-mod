package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.gui.MainScreen;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.RenderableTextModule;
import net.fishinghacks.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

@Module(category = ModuleCategory.UI, name = "server_display")
public class ServerDisplay extends RenderableTextModule {
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
}
