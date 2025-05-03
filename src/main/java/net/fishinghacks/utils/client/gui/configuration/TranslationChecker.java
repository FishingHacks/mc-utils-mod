package net.fishinghacks.utils.client.gui.configuration;

import net.minecraft.client.resources.language.I18n;

public class TranslationChecker {
    public static String getWithFallback(String key, String fallback) {
        if (I18n.exists(key)) return key;
        return fallback;
    }
}
