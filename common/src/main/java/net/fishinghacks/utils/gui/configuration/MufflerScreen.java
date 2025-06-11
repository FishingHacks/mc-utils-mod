package net.fishinghacks.utils.gui.configuration;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.ListScreen;
import net.fishinghacks.utils.gui.components.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

public class MufflerScreen extends ListScreen {
    public boolean asPopup = false;
    protected Button doneButton, undoButton, redoButton, resetButton;
    protected final UndoManager undoManager = new UndoManager();
    protected Input searchInput;

    protected MufflerScreen(Screen parent) {
        super(Translation.ModuleMufflerTitle.get(), parent);
    }

    private void resetValues() {
        var map = Configs.clientConfig.MUFFLER_STATE.get();
        undoManager.add(new UndoManager.Step<HashMap<net.minecraft.resources.ResourceLocation, Integer>>(
            Configs.clientConfig.MUFFLER_STATE::set, new HashMap<>(), Configs.clientConfig.MUFFLER_STATE::set, map));
        rebuildList();
    }

    @Override
    protected void onInit() {
        titleWidget.visible = false;
        removeWidget(titleWidget);

        int y = listStartY - 25;
        int x = listStartX - Button.CUBE_WIDTH - 6;

        doneButton = this.addRenderableWidget(
            Button.Builder.cube("<").onPress((button) -> this.onClose()).pos(x, y).build());
        y += Button.DEFAULT_HEIGHT + 6;
        resetButton = this.addRenderableWidget(
            new IconButton.Builder(Icons.RESET).pos(x, y).onPress(btn -> resetValues()).build());
        y += Button.DEFAULT_HEIGHT + 6;
        undoButton = this.addRenderableWidget(new IconButton.Builder(Icons.UNDO).pos(x, y).onPress(btn -> {
            undoManager.undo();
            rebuildList();
        }).build());
        y += Button.DEFAULT_HEIGHT + 6;
        redoButton = this.addRenderableWidget(new IconButton.Builder(Icons.REDO).pos(x, y).onPress(btn -> {
            undoManager.redo();
            rebuildList();
        }).build());
        searchInput = this.addRenderableWidget(
            new Input.Builder(0, 0, listWidth / 4 * 3, Button.DEFAULT_HEIGHT).hint(Translation.Search.get())
                .responder(ignored -> rebuildList()).value(searchInput == null ? "" : searchInput.getValue()).build());
    }

    @Override
    protected int getListStartY() {
        return super.getListStartY() + 25;
    }

    private boolean matches(String keyStr, @Nullable WeighedSoundEvents instance, String query) {
        if (query.isBlank()) return true;
        if (keyStr.contains(query)) return true;
        if (instance == null || instance.getSubtitle() == null) return false;
        return instance.getSubtitle().getString().toLowerCase().contains(query);
    }

    @Override
    protected void buildList() {
        LinearLayout boxLayout = LinearLayout.vertical().spacing(4);
        listLayout.addChild(new Box(boxLayout, new Box.Borders().setTop(false)));
        int width = listWidth - 2 * Box.DEFAULT_BORDER_SIZE;
        var soundmanager = Minecraft.getInstance().getSoundManager();
        String query = searchInput.getValue().toLowerCase();

        final boolean[] hasChild = {false};
        BuiltInRegistries.SOUND_EVENT.entrySet().stream().map(el -> el.getKey().location()).forEach(key -> {
            Component component;
            String keyStr = key.toString().toLowerCase();
            var instance = soundmanager.getSoundEvent(key);
            if (!matches(keyStr, instance, query)) return;
            hasChild[0] = true;

            if (instance == null || instance.getSubtitle() == null) component = Component.literal(keyStr);
            else component = instance.getSubtitle().copy().append(" (").append(keyStr).append(")");

            boxLayout.addChild(
                new MufflerEntry(component, () -> getVolume(key), volume -> setVolume(key, volume), key, width));
        });
        if (!hasChild[0]) {
            var layout = LinearLayout.horizontal();
            layout.addChild(new Spacer(4, 0));
            layout.addChild(new Text(Translation.NoResults.with(query)));
            boxLayout.addChild(layout);
        }
        boxLayout.addChild(new DummyAbstractWidget()).setWidth(width);

        boxLayout.arrangeElements();
        listLayout.arrangeElements();
    }

    private void setVolume(ResourceLocation sound, int volume) {
        Configs.clientConfig.MUFFLER_STATE.get().put(sound, volume);
        Configs.clientConfig.MUFFLER_STATE.save();
    }

    private int getVolume(ResourceLocation sound) {
        var volume = Configs.clientConfig.MUFFLER_STATE.get().get(sound);
        return volume == null ? 100 : volume;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int y = listStartY - 25;
        if (!asPopup || parent == null) {
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.fill(listStartX, y, listStartX + listWidth, getListStartY(), Box.DEFAULT_BORDER_COLOR);
            guiGraphics.fill(listStartX + 2, y + 2, listStartX + listWidth - 2, listStartY,
                Box.DEFAULT_BACKGROUND_COLOR);
            return;
        }
        parent.render(guiGraphics, -1, -1, partialTick);
        assert this.minecraft != null;
        guiGraphics.flush();
        RenderSystem.getDevice().createCommandEncoder()
            .clearDepthTexture(Objects.requireNonNull(this.minecraft.getMainRenderTarget().getDepthTexture()), 1.0);
        renderTransparentBackground(guiGraphics);

        guiGraphics.fill(listStartX, y, listStartX + listWidth, getListStartY(), Box.DEFAULT_BORDER_COLOR);
        guiGraphics.fill(listStartX + 2, y + 2, listStartX + listWidth - 2, listStartY, Box.DEFAULT_BACKGROUND_COLOR);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int y = listStartY - 25;

        if (undoButton != null) undoButton.active = undoManager.canUndo();
        if (redoButton != null) redoButton.active = undoManager.canRedo();
        else titleWidget.setY((y - titleWidget.getHeight()) / 2);

        searchInput.setX(listStartX + listWidth / 8);
        searchInput.setY(y + 4);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
