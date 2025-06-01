package net.fishinghacks.utils.events;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.gui.GuiOverlayManager;
import net.fishinghacks.utils.modules.ClickUi;
import net.fishinghacks.utils.modules.DragUI;
import net.fishinghacks.utils.modules.ModuleManager;
import net.fishinghacks.utils.modules.RenderableModule;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(ClickUi.CLICK_UI_MAPPING.get());
    }

    @SubscribeEvent
    public static void registerGuiRender(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Constants.id("gui_overlay"), (guiGraphics, delta) -> {
            float partialTick = delta.getGameTimeDeltaPartialTick(true);
            if (Minecraft.getInstance().screen instanceof DragUI) return;
            ModuleManager.enabledModules.forEach(mod -> {
                if (ModuleManager.modules.get(mod) instanceof RenderableModule module)
                    if (module.shouldRender()) module.render(guiGraphics, partialTick);
            });
        });
        event.registerAboveAll(Constants.id("notifications"), (guiGraphics, deltaTracker) -> {
            if (Minecraft.getInstance().screen == null) {
                GuiOverlayManager.repositionNotifications(Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                    Minecraft.getInstance().getWindow().getGuiScaledHeight());
                GuiOverlayManager.renderNotifications(guiGraphics, -1, -1,
                    deltaTracker.getGameTimeDeltaPartialTick(true));
            }
        });
    }
}