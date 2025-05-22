package net.fishinghacks.utils.actions;

import net.fishinghacks.utils.TranslatableEnum;
import net.fishinghacks.utils.Translation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public enum ActionType implements TranslatableEnum {
    ToggleModule, EnableModule, DisableModule, HoldModule;

    @Override
    public @NotNull Component getTranslatedName() {
        return switch (this) {
            case ToggleModule -> Translation.ActionTypeToggleModule.get();
            case EnableModule -> Translation.ActionTypeEnableModule.get();
            case DisableModule -> Translation.ActionTypeDisableModule.get();
            case HoldModule -> Translation.ActionTypeHoldModule.get();
        };
    }

    public Action create() {
        return switch (this) {
            case ToggleModule -> new ToggleModuleAction();
            case EnableModule -> new EnableModuleAction();
            case DisableModule -> new DisableModuleAction();
            case HoldModule -> new HoldModuleAction();
        };
    }
}
