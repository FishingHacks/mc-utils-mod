package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.gui.Icons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MufflerEntry extends Box {
    private final Text text;
    private final Slider volumeSlider;
    private final IconButton muteButton;
    private static final int margin = 4;

    public MufflerEntry(Component name, Supplier<Integer> getVolume, Consumer<Integer> setVolume, int width) {
        super(new Spacer(width, 0), Borders.NONE);
        var minecraft = Minecraft.getInstance();
        text = new Text(name, minecraft.font, 1f);
        text.active = false;
        volumeSlider = new Slider(0, 100, Math.abs(getVolume.get()),
            Component.literal(Math.abs(getVolume.get()) + "%"));
        volumeSlider.setWidth(200);
        muteButton = new IconButton.Builder(getVolume.get() > 0 ? Icons.UNMUTED : Icons.MUTED).build();
        volumeSlider.onChange((ignored, newVolume) -> {
            setVolume.accept(newVolume);
            muteButton.setIcon(Icons.UNMUTED);
            volumeSlider.setMessage(Component.literal(newVolume + "%"));
        });
        muteButton.onPress(ignored -> {
            setVolume.accept(-getVolume.get());
            muteButton.setIcon(getVolume.get() > 0 ? Icons.UNMUTED : Icons.MUTED);
        });
        setHeight(getHeight());
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        text.setX(x + margin);
        int right = x + getWidth() - margin;
        muteButton.setX(right - muteButton.getWidth());
        right -= margin + muteButton.getWidth();
        volumeSlider.setX(right - volumeSlider.getWidth());
        text.setWidth(volumeSlider.getRight() - x - margin);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        int height = getHeight();
        text.setY(y + (height - text.getHeight()) / 2);
        volumeSlider.setY(y + (height - volumeSlider.getHeight()) / 2);
        muteButton.setY(y + (height - muteButton.getHeight()) / 2);
    }

    @Override
    public int getHeight() {
        if (text == null || volumeSlider == null || muteButton == null) return 0;
        int height = Math.max(text.getHeight(), volumeSlider.getHeight());
        return Math.max(super.getHeight(), height);
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
        consumer.accept(text);
        consumer.accept(volumeSlider);
        consumer.accept(muteButton);
    }
}
