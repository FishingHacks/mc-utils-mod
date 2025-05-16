package net.fishinghacks.utils.gui;

import com.mojang.blaze3d.FieldsAreNonnullByDefault;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fishinghacks.utils.ClientConstants;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.components.Button;
import net.fishinghacks.utils.gui.components.Notification;
import net.fishinghacks.utils.gui.mcsettings.McSettingsScreen;
import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.Translation;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuiOverlayManager {
    @Nullable
    private static Overlay overlay;
    private static final List<Notification> notifications = new ArrayList<>();
    private static boolean repositionNotifications = false;
    private static final Button serviceServerSettings = Button.Builder.big(Translation.ServerConnection.get()).y(2)
        .onPress(ignored -> ClientConnectionHandler.openSettingsScreen()).build();
    private static final int serviceServerTextY = 2 + Button.DEFAULT_HEIGHT + 2;

    public static @Nullable Overlay getOverlay() {
        return overlay;
    }

    public static void setOverlay(@Nullable Overlay overlay) {
        GuiOverlayManager.overlay = overlay;
    }

    public static void setOverlay(AbstractWidget owner, ScreenRectangle rectangle, Render render) {
        setOverlay(new Overlay(owner, render, rectangle));
    }

    public static void setOverlay(AbstractWidget owner, int x, int y, int width, int height, Render render) {
        setOverlay(new Overlay(owner, render, new ScreenRectangle(x, y, width, height)));
    }

    public static void renderPost(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, Screen screen) {
        graphics.flush();
        RenderSystem.getDevice().createCommandEncoder()
            .clearDepthTexture(Objects.requireNonNull(Minecraft.getInstance().getMainRenderTarget().getDepthTexture()),
                1.0);

        if (overlay != null) overlay.render.render(graphics, mouseX, mouseY, partialTick);
        renderNotifications(graphics, mouseX, mouseY, partialTick);
        if (shouldRenderConnectedServerOverlay(screen))
            renderConnectedServerOverlay(graphics, mouseX, mouseY, partialTick, screen.width);
    }

    private static boolean firstRender = true;

    public static boolean renderPre(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, Screen screen) {
        if(mouseX == -1 && mouseY == -1) return false;
        if (firstRender) {
            firstRender = false;
            ClientConstants.onFirstGuiRender();
        }
        if (repositionNotifications) repositionNotifications(screen.width, screen.height);
        boolean mouseOverlaps = isInOverlay(mouseX, mouseY);
        overlay = null;

        // this causes the screen to think the mouse is not where it is. we still render the overlay later so that's
        // fine
        if (mouseOverlaps) screen.render(graphics, -1, -1, partialTick);
        return mouseOverlaps;
    }

    public static boolean onClick(int mouseX, int mouseY, int button) {
        if (overlay != null && overlay.rectangle.containsPoint(mouseX, mouseY)) {
            overlay.owner.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        for (Notification notification : notifications) {
            if (notification.getRectangle().containsPoint(mouseX, mouseY)) {
                notification.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }
        if (shouldRenderConnectedServerOverlay(Minecraft.getInstance().screen) && serviceServerSettings.getRectangle()
            .containsPoint(mouseX, mouseY)) {
            serviceServerSettings.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    public static void renderNotifications(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int i = 0;
        while (i < notifications.size()) {
            notifications.get(i).render(graphics, mouseX, mouseY, partialTick);
            if (notifications.get(i).checkForRemoval() && i < notifications.size()) notifications.remove(i);
            else ++i;
        }
    }

    public static Notification addNotification(Component message, Notification.NotifyButton... buttons) {
        return addNotification(message, List.of(buttons));
    }

    public static Notification addNotification(Component message, List<Notification.NotifyButton> buttons) {
        return addNotification(message, buttons, Duration.ofSeconds(15));
    }

    public static Notification addNotification(Component message, List<Notification.NotifyButton> buttons,
                                               Duration openDuration) {
        Notification notification = new Notification(message, buttons, openDuration);
        notifications.add(notification);
        repositionNotifications = true;
        return notification;
    }

    public static void removeNotification(Notification notification) {
        for (int i = 0; i < notifications.size(); ++i) {
            if (notifications.get(i).equals(notification)) {
                notifications.remove(i);
                repositionNotifications = true;
                return;
            }
        }
    }

    private static boolean isInOverlay(int x, int y) {
        if (overlay != null && overlay.rectangle.containsPoint(x, y)) return true;
        if (serviceServerSettings.getRectangle().containsPoint(x, y) && shouldRenderConnectedServerOverlay(
            Minecraft.getInstance().screen)) return true;
        for (Notification notification : notifications) {
            if (notification.getRectangle().containsPoint(x, y)) return true;
        }
        return false;
    }

    private static void repositionNotifications(int width, int height) {
        width -= 2;
        height -= 2;
        for (Notification notification : notifications) {
            notification.setPosition(width - notification.getWidth(), height - notification.getHeight());
            height -= notification.getHeight() + 2;
        }
    }

    private static boolean shouldRenderConnectedServerOverlay(@Nullable Screen screen) {
        if (screen == null) return false;
        return (screen instanceof MainScreen) || (screen instanceof TitleScreen) || (screen instanceof OptionsScreen) || (screen instanceof McSettingsScreen) || (screen instanceof PauseScreen) || (screen instanceof PauseMenuScreen);
    }

    private static void renderConnectedServerOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                                     int screenWidth) {
        serviceServerSettings.setX(screenWidth - 2 - serviceServerSettings.getWidth());
        serviceServerSettings.render(RenderType.guiOverlay(), graphics, mouseX, mouseY);
        Component message = ClientConnectionHandler.getInstance().getFormattedStatus();
        Font font = Minecraft.getInstance().font;
        int width = font.width(message);
        graphics.drawString(font, message, screenWidth - 2 - width, serviceServerTextY, Colors.WHITE.get());
    }

    public static boolean onToast(Toast toast) {
        if (!Configs.clientConfig.REPLACE_SYSTEM_TOASTS.get()) return false;
        return (toast instanceof SystemToast);
    }

    public record Overlay(AbstractWidget owner, Render render, ScreenRectangle rectangle) {
    }

    @FunctionalInterface
    public interface Render {
        void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
    }
}
