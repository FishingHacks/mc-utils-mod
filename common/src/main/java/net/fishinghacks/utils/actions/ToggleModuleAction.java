package net.fishinghacks.utils.actions;

import net.fishinghacks.utils.modules.ModuleManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.fishinghacks.utils.actions.DisableModuleAction.MODULES;

public class ToggleModuleAction extends Action {
    protected String module = "";

    @Override
    public ActionType type() {
        return ActionType.ToggleModule;
    }

    @Override
    protected void enable() {
        ModuleManager.toggleModule(module);
    }

    @Override
    protected void disable() {
    }

    @Override
    public String getValue() {
        return module;
    }

    @Override
    public void setValue(String value) {
        module = value;
    }

    @Override
    public @Nullable List<String> validValues() {
        return MODULES.get();
    }

    @Override
    public Component formatValue(String value) {
        return value.isEmpty() ? Component.literal("") : Component.translatable("utils.configuration." + value);
    }
}
