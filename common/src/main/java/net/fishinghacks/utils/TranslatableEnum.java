package net.fishinghacks.utils;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface TranslatableEnum {
    @NotNull Component getTranslatedName();
}
