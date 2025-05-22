package net.fishinghacks.utils.mixin_misc;

import net.minecraft.network.chat.Component;

public record DisplayEntry(Component name, Component score, int scoreWidth) {
}
