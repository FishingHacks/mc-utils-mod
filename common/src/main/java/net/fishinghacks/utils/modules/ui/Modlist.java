package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.ModuleManager;
import net.fishinghacks.utils.modules.RenderableTextModule;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

@Module(name = "modlist", category = ModuleCategory.UI)
public class Modlist extends RenderableTextModule {
    @Override
    public List<Component> getText() {
        return ModuleManager.enabledModules.stream().map(name -> Component.translatable("utils.configuration." + name))
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Component> getPreviewText() {
        return List.of(Component.literal("ClickUI"));
    }
}
