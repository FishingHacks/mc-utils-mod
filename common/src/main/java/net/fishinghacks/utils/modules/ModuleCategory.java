package net.fishinghacks.utils.modules;

import net.fishinghacks.utils.Translation;
import net.minecraft.network.chat.Component;

public enum ModuleCategory {
    UI(Translation.ModuleCategoryUi.get(), "ui"),
    MISC(Translation.ModuleCategoryMisc.get(), "misc");

    public final Component component;
    public final String name;

    ModuleCategory(Component component, String name) {
        this.component = component;
        this.name = name;
    }
}
