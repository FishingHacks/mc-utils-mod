package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.actions.Action;
import net.fishinghacks.utils.actions.ActionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ActionListEntry extends Box {
    private final AbstractWidget[] children;
    private static final int margin = 4;

    public ActionListEntry(Action action, int index, List<Button> keybindButtons, BiConsumer<Integer, ActionType> updateType,
                           BiConsumer<Button, Integer> onKeybindPress, Consumer<Integer> removeEntry,
                           BiConsumer<Integer, String> setValue, int width) {
        super(new Spacer(width - 2 * DEFAULT_BORDER_SIZE, 0));
        var minecraft = Minecraft.getInstance();
        var typeText = new Text(Translation.GuiActionsType.get(), minecraft.font, 1f);
        var typeDropdown = GuiDropdown.fromTranslatableEnum(action.type(), ActionType.values());
        typeDropdown.onValueChange((ignored, value) -> updateType.accept(index, value));
        typeDropdown.setWidth(Input.DEFAULT_WIDTH_BIG);
        var keyText = new Text(Translation.GuiActionsKey.get(), minecraft.font, 1f);
        var keybindButton = Button.Builder.small(action.key().getDisplayName()).width(Input.DEFAULT_WIDTH_BIG)
            .onPress(button -> onKeybindPress.accept(button, index)).build();
        var valueText = new Text(Translation.GuiActionsValue.get(), minecraft.font, 1f);
        AbstractWidget valueWidget;
        var validValues = action.validValues();
        if (validValues == null)
            valueWidget = Input.Builder.big().value(action.getValue()).responder(s -> setValue.accept(index, s))
                .build();
        else valueWidget = new GuiDropdown<>(action.getValue(), action::formatValue,
            (ignored, v) -> setValue.accept(index, v), validValues);
        var removeButton = Button.Builder.small(Translation.GuiActionsRemove.get())
            .onPress(ignored -> removeEntry.accept(index)).build();
        children = new AbstractWidget[]{typeText, typeDropdown, keyText, keybindButton, valueText, valueWidget,
            removeButton};

        keybindButtons.add(keybindButton);
        setHeight(getHeight());
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        if (this.children == null) return;
        x += margin;
        for (var c : this.children) {
            c.setX(x);
            x += margin + c.getWidth();
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        if (this.children == null) return;
        int height = getHeight();
        for (var c : this.children) {
            c.setY(y + (height - c.getHeight()) / 2);
        }
    }

    @Override
    public int getHeight() {
        if (this.children == null) return margin * 2;
        int height = 0;
        for (var c : this.children) height = Math.max(height, c.getHeight());
        return Math.max(super.getHeight(), height + margin * 2);
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
        for (var c : children) consumer.accept(c);
    }
}
