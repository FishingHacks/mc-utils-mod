package net.fishinghacks.utils.actions;

import net.fishinghacks.utils.modules.ModuleManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HoldModuleAction extends Action {
    protected String module = "";

    @Override
    public ActionType type() {
        return ActionType.HoldModule;
    }

    @Override
    protected void enable() {
        ModuleManager.enableModule(module);
    }

    @Override
    protected void disable() {
        ModuleManager.disableModule(module);
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
        return DisableModuleAction.MODULES.get();
    }

    @Override
    public Component formatValue(String value) {
        return value.isEmpty() ? Component.literal("") : Component.translatable("utils.configuration." + value);
    }
}
