package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.parsing.TokenType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


public class Highlighter {
    private final String lineContents;
    private final MutableComponent output = Component.empty();
    private int pos = 0;
    private HighlightType type = HighlightType.None;
    private StringBuilder accumulator = new StringBuilder();

    private Highlighter(String lineContents) {
        this.lineContents = lineContents;
    }

    public static Component highlightLine(String line) {
        var highlighter = new Highlighter(line);
        while (highlighter.pos < highlighter.lineContents.length()) highlighter.highlightNextCharacter();
        if (!highlighter.accumulator.isEmpty()) highlighter.output.append(
            Component.literal(highlighter.accumulator.toString()).withStyle(highlighter.type.format));
        return highlighter.output;
    }

    private char peek() {
        return pos < lineContents.length() ? lineContents.charAt(pos) : '\0';
    }

    private char next() {
        return lineContents.charAt(pos++);
    }

    private void applyStyle(HighlightType type) {
        if (this.type == type) return;
        if (accumulator.isEmpty()) {
            this.type = type;
            return;
        }
        output.append(Component.literal(accumulator.toString()).withStyle(this.type.format));
        accumulator = new StringBuilder();
        this.type = type;
    }

    private void skipNumber() {
        while (peek() >= '0' && peek() <= '9') {
            applyStyle(HighlightType.Number);
            accumulator.append(next());
        }
    }

    private void skipString() {
        boolean isEscaping = false;
        while (true) {
            if (pos >= lineContents.length()) return;
            var c = next();
            if (isEscaping) {
                applyStyle(HighlightType.EscapeCode);
                accumulator.append(c);
                isEscaping = false;
            } else if (c == '\\') {
                applyStyle(HighlightType.EscapeCode);
                accumulator.append(c);
                isEscaping = true;
            } else if (c == '"') {
                applyStyle(HighlightType.String);
                accumulator.append(c);
                return;
            } else {
                applyStyle(HighlightType.String);
                accumulator.append(c);
            }
        }
    }

    private String getIdentifier(char c) {
        var builder = new StringBuilder();
        builder.append(c);
        while ((peek() >= 'a' && peek() <= 'z') || (peek() >= 'A' && peek() <= 'Z')) builder.append(next());
        return builder.toString();
    }

    private void highlightNextCharacter() {
        if (pos >= lineContents.length()) return;
        char c = next();
        switch (c) {
            case '"' -> {
                applyStyle(HighlightType.String);
                accumulator.append(c);
                skipString();
            }
            case '(', ')', '[', ']', '{', '}' -> {
                applyStyle(HighlightType.Parens);
                accumulator.append(c);
            }
            default -> {
                if (c == '/' && peek() == '/') {
                    applyStyle(HighlightType.Comment);
                    accumulator.append(c);
                    // this is fine because we work on a *single* line at a time.
                    while (pos < lineContents.length()) accumulator.append(next());
                } else if (TokenType.fromSingleCharacter(c).isPresent()) {
                    applyStyle(HighlightType.Operator);
                    accumulator.append(c);
                } else if (c >= '0' && c <= '9') {
                    applyStyle(HighlightType.Number);
                    accumulator.append(c);
                    skipNumber();
                } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    var identifier = getIdentifier(c);
                    if (TokenType.fromKeyword(identifier).isPresent()) applyStyle(HighlightType.Keyword);
                    else applyStyle(HighlightType.None);
                    accumulator.append(identifier);
                } else {
                    applyStyle(HighlightType.None);
                    accumulator.append(c);
                }
            }
        }
    }

    private enum HighlightType {
        None(ChatFormatting.WHITE), String(ChatFormatting.YELLOW), Number(ChatFormatting.GREEN), Comment(
            ChatFormatting.GRAY), Operator(ChatFormatting.AQUA), Parens(ChatFormatting.DARK_AQUA), EscapeCode(
            ChatFormatting.GOLD), Keyword(ChatFormatting.LIGHT_PURPLE);

        public final ChatFormatting format;

        HighlightType(ChatFormatting format) {
            this.format = format;
        }
    }
}
