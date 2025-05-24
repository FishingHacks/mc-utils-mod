package net.fishinghacks.utils.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.actions.ActionType;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.gui.components.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ActionsListScreen extends ListScreen {
    public ActionsListScreen(Screen parent) {
        super(Translation.GuiActionsTitle.get(), parent);
    }

    private @Nullable Integer selectedKey = null;
    private final List<Button> buttons = new ArrayList<>();

    @Override
    protected void onInit() {
        int y = listStartY;
        int x = listStartX - Button.CUBE_WIDTH - 6;

        y += this.addRenderableWidget(Button.Builder.cube("<").onPress((button) -> this.onClose()).pos(x, y).build())
            .getHeight() + 6;
        this.addRenderableWidget(Button.Builder.cube("+").onPress((button) -> this.addEntry()).pos(x, y).build())
            .getHeight();
    }

    @Override
    protected void buildList() {
        assert minecraft != null;
        listLayout = new LinearLayout(listStartX, listStartY, LinearLayout.Orientation.VERTICAL).spacing(2);

        buttons.clear();
        int i = 0;
        for (var action : Configs.clientConfig.ACTIONS.get()) {
            final int idx = i;

            listLayout.addChild(new ActionListEntry(action, idx, buttons, this::updateType, (selfButton, index) -> {
                if (index.equals(selectedKey)) return;
                if (selectedKey != null && buttons.size() < selectedKey) {
                    var list = Configs.clientConfig.ACTIONS.get();
                    if (list.size() < selectedKey) buttons.get(selectedKey)
                        .setMessage(Configs.clientConfig.ACTIONS.get().get(selectedKey).key().getDisplayName());
                    else repositionElements();
                }
                selectedKey = index;
                selfButton.setMessage(Component.literal("> ")
                    .append(selfButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                    .append(" <").withStyle(ChatFormatting.YELLOW));
            }, this::removeEntry, this::setValue, listWidth));
            listLayout.addChild(new Spacer(0, 10));
            ++i;
        }
    }

    private void setValue(int index, String value) {
        var list = Configs.clientConfig.ACTIONS.get();
        if (index < list.size()) list.get(index).setValue(value);
        Configs.clientConfig.ACTIONS.set(list, false);
    }

    private void removeEntry(int index) {
        var list = Configs.clientConfig.ACTIONS.get();
        if (index < list.size()) list.remove(index);
        Configs.clientConfig.ACTIONS.set(list, false);
        rebuildList();
    }

    private void updateType(int index, ActionType type) {
        var list = Configs.clientConfig.ACTIONS.get();
        if (list.size() <= index) return;

        var value = list.get(index);
        var newValue = type.create();
        newValue.setKey(value.key());
        var validValues = newValue.validValues();
        if (validValues == null || validValues.contains(value.getValue())) newValue.setValue(value.getValue());
        list.set(index, newValue);
        Configs.clientConfig.ACTIONS.set(list, false);
        rebuildList();
    }

    @Override
    public void onClose() {
        Configs.clientConfig.ACTIONS.save();
        super.onClose();
    }

    private void addEntry() {
        var list = Configs.clientConfig.ACTIONS.get();
        list.add(ActionType.ToggleModule.create());
        Configs.clientConfig.ACTIONS.set(list, false);
        rebuildList();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedKey == null) return super.mouseClicked(mouseX, mouseY, button);
        var list = Configs.clientConfig.ACTIONS.get();
        if (selectedKey >= list.size()) {
            selectedKey = null;
            repositionElements();
            return true;
        }
        list.get(selectedKey).setKey(InputConstants.Type.MOUSE.getOrCreate(button));
        if (selectedKey < buttons.size())
            buttons.get(selectedKey).setMessage(list.get(selectedKey).key().getDisplayName());
        Configs.clientConfig.ACTIONS.set(list, false);
        selectedKey = null;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedKey == null) return super.keyPressed(keyCode, scanCode, modifiers);
        var list = Configs.clientConfig.ACTIONS.get();
        if (selectedKey >= list.size()) {
            selectedKey = null;
            repositionElements();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) list.get(selectedKey).setKey(InputConstants.UNKNOWN);
        else list.get(selectedKey).setKey(InputConstants.getKey(keyCode, scanCode));
        if (selectedKey < buttons.size())
            buttons.get(selectedKey).setMessage(list.get(selectedKey).key().getDisplayName());
        Configs.clientConfig.ACTIONS.set(list, false);
        selectedKey = null;
        return true;
    }
}
