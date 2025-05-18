package net.fishinghacks.utils.gui.configuration;

import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigSpec;
import net.minecraft.client.gui.screens.Screen;

public record ConfigContext(Screen parent, ConfigSpec spec, Config config) {
}
