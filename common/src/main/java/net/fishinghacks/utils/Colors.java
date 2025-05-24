package net.fishinghacks.utils;

import net.fishinghacks.utils.config.values.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

public enum Colors {
    WHITE(ChatFormatting.WHITE), BLACK(0), BG_DARK(0x16, 0x16, 0x16), DARK_HOVER(0x26, 0x26, 0x26), DARK(0x36, 0x36,
        0x36), DARK_HIGHLIGHT(0x46, 0x46, 0x46), DARK_SELECTED(ChatFormatting.DARK_GRAY), DARK_DISABLED(0x70, 0x70,
        0x70), GRAY(0x88, 0x88, 0x88), DARK_GRAY(0x44, 0x44, 0x44), LIGHT_GRAY(0xaa, 0xaa, 0xaa), CYAN(
        ChatFormatting.AQUA), PRIMARY(0x5e, 0x1e, 0xcc), SECONDARY(0x8A, 0x5B, 0xDA), SECONDARY_LIGHT(0xa1, 0x6b,
        0xff), SECONDARY_DARK(0x79, 0x44, 0xd5), RED(ChatFormatting.RED);

    private static int assertNonNull(@Nullable Integer c) {
        assert c != null;
        return c;
    }

    private final int color;

    Colors(ChatFormatting formatting) {
        this(assertNonNull(formatting.getColor()));
    }

    Colors(int r, int g, int b) {
        this(ARGB.color(r, g, b));
    }

    Colors(int color) {
        this.color = (color & 0xffffff) | 0xff000000;
    }

    public int get() {
        return color;
    }

    public int withAlpha(int alpha) {
        return (this.color & 0xffffff) | ((alpha & 0xff) << 24);
    }

    public Color toCol() {
        return Color.fromARGB(color);
    }
}
