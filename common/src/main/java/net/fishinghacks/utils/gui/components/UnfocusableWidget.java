package net.fishinghacks.utils.gui.components;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

public abstract class UnfocusableWidget extends AbstractWidget {
    public UnfocusableWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent ignored) {
        return null;
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        return null;
    }
}
