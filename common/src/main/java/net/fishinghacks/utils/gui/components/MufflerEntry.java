package net.fishinghacks.utils.gui.components;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.gui.Icons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MufflerEntry extends Box {
    private final Slider volumeSlider;
    private final IconButton muteButton;
    private final IconButton playButton;
    private final ResourceLocation sound;
    private static final int margin = 4;

    public MufflerEntry(Component name, Supplier<Integer> getVolume, Consumer<Integer> setVolume,
                        ResourceLocation sound, int width) {
        super(new Spacer(width, 0), Borders.NONE);
        this.sound = sound;
        volumeSlider = new Slider(0, 100, Math.abs(getVolume.get()),
            Component.empty().append(name).append(" " + getVolume.get() + "%"));
        volumeSlider.setWidth(200);
        muteButton = new IconButton.Builder(getVolume.get() > 0 ? Icons.UNMUTED : Icons.MUTED).build();
        playButton = new IconButton.Builder(Icons.OPEN).onPress(this::playSound).build();
        volumeSlider.onChange((ignored, newVolume) -> {
            setVolume.accept(newVolume);
            muteButton.setIcon(Icons.UNMUTED);
            volumeSlider.setMessage(Component.empty().append(name).append(" " + getVolume.get() + "%"));
        });
        muteButton.onPress(ignored -> {
            setVolume.accept(-getVolume.get());
            muteButton.setIcon(getVolume.get() > 0 ? Icons.UNMUTED : Icons.MUTED);
        });
        setHeight(getHeight());
    }

    private void playSound(Button ignored) {
        var event = BuiltInRegistries.SOUND_EVENT.get(sound);
        if (event.isEmpty()) {
            Constants.LOG.info("No sound even present for {}", sound);
            return;
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event.get().value(), 1f, 1f));
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        int right = x + getWidth() - margin;
        playButton.setX(right - playButton.getWidth());
        right -= margin + playButton.getWidth();
        muteButton.setX(right - muteButton.getWidth());
        right -= margin + muteButton.getWidth();
        volumeSlider.setX(x + margin);
        volumeSlider.setWidth(right - x - margin);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        int height = getHeight();
        volumeSlider.setY(y + (height - volumeSlider.getHeight()) / 2);
        muteButton.setY(y + (height - muteButton.getHeight()) / 2);
        playButton.setY(y + (height - playButton.getHeight()) / 2);
    }

    @Override
    public int getHeight() {
        if (volumeSlider == null || muteButton == null) return 0;
        int height = Math.max(Math.max(muteButton.getHeight(), playButton.getHeight()), volumeSlider.getHeight());
        return Math.max(super.getHeight(), height);
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
        consumer.accept(volumeSlider);
        consumer.accept(muteButton);
        consumer.accept(playButton);
    }
}
