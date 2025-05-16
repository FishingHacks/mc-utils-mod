package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.RenderableTextModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class PingModule extends RenderableTextModule {
    private static long ping() {
        ServerData data = Minecraft.getInstance().getCurrentServer();
        if(data == null) return 0;
        return data.ping;
    }

    @Override
    public List<Component> getText() {
        assert Minecraft.getInstance().player != null;
        long ping = ping();
        MutableComponent comp = Component.literal(ping+"ms").withStyle(ChatFormatting.GREEN);
        if(ping > 700) comp = comp.withStyle(ChatFormatting.RED);
        else if(ping > 350) comp = comp.withStyle(ChatFormatting.YELLOW);
        else comp = comp.withStyle(ChatFormatting.GREEN);
        return List.of(Component.literal("Ping: ").withStyle(ChatFormatting.GRAY).append(comp));
    }

    @Override
    public List<Component> getPreviewText() {
        return List.of(Component.literal("Ping: ").withStyle(ChatFormatting.GRAY).append(Component.literal("0ms").withStyle(ChatFormatting.GREEN)));
    }

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.UI;
    }
}
