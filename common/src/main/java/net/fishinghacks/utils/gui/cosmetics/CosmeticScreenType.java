package net.fishinghacks.utils.gui.cosmetics;

import net.fishinghacks.utils.TranslatableEnum;
import net.fishinghacks.utils.Translation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public enum CosmeticScreenType implements TranslatableEnum {
    MinecraftCapes(Translation.CosmeticGuiTypeMCCapes), ServerCapes(
        Translation.CosmeticGuiTypeServerCapes), ServerModels(Translation.CosmeticGuiTypeServerModels);

    public final Component translation;

    CosmeticScreenType(Translation translation) {
        this.translation = translation.get();
    }

    @Override
    public @NotNull Component getTranslatedName() {
        return translation;
    }
}
