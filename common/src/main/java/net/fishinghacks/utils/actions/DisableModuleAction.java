package net.fishinghacks.utils.actions;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.fishinghacks.utils.modules.ModuleManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DisableModuleAction extends Action {
    protected String module = "";
    public static final Supplier<List<String>> MODULES = Suppliers.memoize(() -> {
        List<String> modules = new ArrayList<>();
        modules.add("");
        modules.addAll(ModuleManager.modules.keySet());
        return modules;
    });

    @Override
    public ActionType type() {
        return ActionType.DisableModule;
    }

    @Override
    protected void enable() {
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
        return MODULES.get();
    }

    @Override
    protected void disable() {
    }

    @Override
    public Component formatValue(String value) {
        return value.isEmpty() ? Component.literal("") : Component.translatable("utils.configuration." + value);
    }
}
