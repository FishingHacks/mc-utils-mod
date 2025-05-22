package net.fishinghacks.utils.config.values;

import net.minecraft.util.ARGB;

import java.util.Optional;

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

    /// h ([0]):  [0;360]
    /// s ([1]):  [0;1]
    /// l ([2]):  [0;1]
    /// a ([3]):  [0;255]
    @SuppressWarnings("SpellCheckingInspection")
    public float[] hsva() {
        float r = (float) r() / 255;
        float g = (float) g() / 255;
        float b = (float) b() / 255;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        int hue;
        float saturation = max == 0 ? 0f : delta / max;
        if (delta == 0f) hue = 0;
        else if (max == r) hue = (int) (60f * (((g - b) / delta) % 6));
        else if (max == g) hue = (int) (60f * (((b - r) / delta) + 2));
        else hue = (int) (60f * (((r - g) / delta) + 4));

        return new float[]{hue, saturation, max, (float) a()};
    }

    public String format() {
        var s = "#" + toHex(r()) + toHex(g()) + toHex(b());
        if (a() != 0xff) return s + toHex(a());
        return s;
    }

    public Color withAlpha(int alpha) {
        return new Color(ARGB.color(alpha, this.col));
    }


    public Color withAlpha(float alpha) {
        return new Color(ARGB.color((int) (alpha * 255f), this.col));
    }

    private Color(int col) {
        this.col = col;
    }

    public Color(int r, int g, int b, int a) {
        this(ARGB.color(a, r, g, b));
    }

    private Color(float r, float g, float b, int a) {
        this(ARGB.color(a, (int) (r * 255f), (int) (g * 255f), (int) (b * 255f)));
    }

    public static Color fromARGB(int argb) {
        return new Color(argb);
    }

    /// h: [0;360]
    /// s: [0;1]
    /// l: [0;1]
    /// a: [0;255]
    public static Color fromHSVA(int h, float s, float v, int a) {
        float chroma = v * s;
        float x = chroma * (1 - Math.abs((((float) h / 60f) % 2f) - 1f));
        float m = v - chroma;
        if (h <= 60) return new Color(chroma + m, x + m, m, a);
        else if (h <= 120) return new Color(x + m, chroma + m, m, a);
        else if (h <= 180) return new Color(m, chroma + m, x + m, a);
        else if (h <= 240) return new Color(m, x + m, chroma + m, a);
        else if (h <= 300) return new Color(x + m, m, chroma + m, a);
        else return new Color(chroma + m, m, x + m, a);
    }

    // #rgb, #rgba, #rrggbb, #rrggbbaa
    public static Optional<Color> parse(String input) {
        if (!input.startsWith("#")) return Optional.empty();
        return switch (input.length()) {
            case 4 -> {
                var r = parseHexSingle(input, 1);
                var g = parseHexSingle(input, 2);
                var b = parseHexSingle(input, 3);
                if (r.isEmpty() || g.isEmpty() || b.isEmpty()) yield Optional.empty();
                yield Optional.of(new Color(dup(r.get()), dup(g.get()), dup(b.get()), 0xff));
            }
            case 5 -> {
                var r = parseHexSingle(input, 1);
                var g = parseHexSingle(input, 2);
                var b = parseHexSingle(input, 3);
                var a = parseHexSingle(input, 4);
                if (r.isEmpty() || g.isEmpty() || b.isEmpty() || a.isEmpty()) yield Optional.empty();
                yield Optional.of(new Color(dup(r.get()), dup(g.get()), dup(b.get()), dup(a.get())));
            }
            case 7 -> {
                var r = parseHex(input, 1);
                var g = parseHex(input, 3);
                var b = parseHex(input, 5);
                if (r.isEmpty() || g.isEmpty() || b.isEmpty()) yield Optional.empty();
                yield Optional.of(new Color(r.get(), g.get(), b.get(), 0xff));
            }
            case 9 -> {
                var r = parseHex(input, 1);
                var g = parseHex(input, 3);
                var b = parseHex(input, 5);
                var a = parseHex(input, 7);
                if (r.isEmpty() || g.isEmpty() || b.isEmpty() || a.isEmpty()) yield Optional.empty();
                yield Optional.of(new Color(r.get(), g.get(), b.get(), a.get()));
            }
            default -> Optional.empty();
        };
    }

    private static int dup(int i) {
        return i | (i << 4);
    }

    private static Optional<Integer> parseHex(String input, int index) {
        if (index + 1 >= input.length()) return Optional.empty();
        var first = parseHexSingle(input.charAt(index));
        var second = parseHexSingle(input.charAt(index + 1));
        if (first.isEmpty() || second.isEmpty()) return Optional.empty();
        return Optional.of((first.get() << 4) | second.get());
    }

    private static Optional<Integer> parseHexSingle(String input, int index) {
        return index >= input.length() ? Optional.empty() : parseHexSingle(input.charAt(index));
    }

    private static Optional<Integer> parseHexSingle(char c) {
        if (c >= '0' && c <= '9') return Optional.of(c - '0');
        if (c >= 'a' && c <= 'f') return Optional.of(c - 'a' + 0xa);
        if (c >= 'A' && c <= 'F') return Optional.of(c - 'A' + 0xa);
        return Optional.empty();
    }

    private static String toHex(int v) {
        int first = (v & 0xf0) >> 4;
        int second = v & 0xf;
        char firstChar = (char) (first <= 9 ? '0' + first : 'a' + first - 0xa);
        char secondChar = (char) (second <= 9 ? '0' + second : 'a' + (second - 0xa));
        return "" + firstChar + secondChar;
    }
}