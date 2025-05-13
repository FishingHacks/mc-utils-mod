package net.fishinghacks.utils.client.gui;

import net.fishinghacks.utils.client.gui.components.Button;
import net.fishinghacks.utils.client.gui.components.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class PopupScreen extends OverlayScreen {
    public final Component message;

    protected PopupScreen(@Nullable Screen parent, Component message) {
        super(Component.empty(), parent, 250, 80);
        this.message = message;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.Builder.normal(CommonComponents.GUI_OK)
            .pos((width - Button.DEFAULT_WIDTH) / 2, getBottom() - Button.DEFAULT_HEIGHT - 5)
            .onPress(ignored -> onClose()).build());
        Text text = new Text(overlayWidth - 20, overlayHeight - 50, message);
        text.setPosition(getX() + 10, getY() + 10);
        text.setLeftAlign(false);
        text.setTopAlign(false);
        this.addRenderableOnly(text);
    }

    public static void popup(Component message) {
        Minecraft.getInstance().setScreen(new PopupScreen(Minecraft.getInstance().screen, message));
    }
}
