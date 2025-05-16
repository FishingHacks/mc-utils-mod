package net.fishinghacks.utils.mixin.client;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(OptionInstance.class)
public interface OptionInstanceAccessor {
    @Accessor
    Component getCaption();
    @Accessor
    Function<Object, Component> getToString();
}
