package net.fishinghacks.utils.config.values;

import net.minecraft.util.ARGB;

public class Color {
    private final int col;

    public int r() {
        return ARGB.red(col);
    }

    public int g() {
        return ARGB.green(col);
    }

    public int b() {
        return ARGB.blue(col);
    }

    public int a() {
        return ARGB.alpha(col);
    }

    public int argb() {
        return col;
    }

    public int abgr() {
        return ARGB.toABGR(col);
    }

    public Color withAlpha(int alpha) {
        return new Color(ARGB.color(alpha, this.col));
    }

    private Color(int col) {
        this.col = col;
    }

    public Color(int r, int g, int b, int a) {
        this(ARGB.color(a, r, g, b));
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 0xff);
    }

    public static Color fromARGB(int argb) {
        return new Color(argb);
    }

    public static Color fromABGR(int abgr) {
        return new Color(ARGB.fromABGR(abgr));
    }
}