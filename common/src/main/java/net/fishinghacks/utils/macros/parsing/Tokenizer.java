package net.fishinghacks.utils.macros.parsing;

import net.fishinghacks.utils.macros.MacroException;
import net.fishinghacks.utils.macros.Translation;

import java.util.Optional;

public class Tokenizer {
    private final String content;
    private final String file;
    public final Location endLoc;
    private int line = 1;
    private int character = 0;
    public int pos = 0;

    public Tokenizer(String content, String file) {
        this.content = content;
        final int[] lastLine = {0};
        final boolean[] lastIsEmpty = {true};
        content.lines().forEach(v -> {
            lastLine[0]++;
            lastIsEmpty[0] = v.isBlank();
        });
        if (!lastIsEmpty[0]) lastLine[0]++;
        endLoc = new Location(file, lastLine[0], 0);
        this.file = file;
    }

    private char peek() {
        return pos < content.length() ? content.charAt(pos) : '\0';
    }

    private char peekpeek() {
        return pos + 1 < content.length() ? content.charAt(pos + 1) : '\0';
    }

    private char next() {
        if (pos >= content.length()) return '\0';
        char c = content.charAt(pos++);
        if (c == '\n') {
            line++;
            character = 0;
        } else character++;
        return c;
    }

    private Location loc() {
        return new Location(file, line, character);
    }

    private boolean isNumberCharacter(char character) {
        return character >= '0' && character <= '9';
    }

    private boolean isIdentifierCharacter(char character) {
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || (character >= '0' && character <= '9') || character == '_';
    }

    public Token parseNumber() {
        boolean hasDot = false;
        StringBuilder numberContent = new StringBuilder();
        if (peek() == '.') {
            hasDot = true;
            numberContent.append("0.");
            next();
        } else numberContent.append(next());
        var loc = loc();
        while (isNumberCharacter(peek()) || (!hasDot && peek() == '.')) numberContent.append(next());
        return new Token(TokenType.Number, numberContent.toString(), loc);
    }

    public Token parseIdentifier() {
        StringBuilder contents = new StringBuilder("" + next());
        var loc = loc();
        while (isIdentifierCharacter(peek())) contents.append(next());
        var buildContents = contents.toString();
        var keyword = TokenType.fromKeyword(buildContents);
        return keyword.map(tokenType -> new Token(tokenType, "", loc))
            .orElseGet(() -> new Token(TokenType.Identifier, buildContents, loc));
    }

    public Token parseString() {
        next();
        var loc = loc();
        StringBuilder accumulator = new StringBuilder();
        boolean isEscape = false;
        while (true) {
            char c = next();
            if (isEscape) {
                switch (c) {
                    case '0' -> accumulator.append('\0');
                    case 'n' -> accumulator.append('\n');
                    default -> accumulator.append(c);
                }
                isEscape = false;
            } else if (c == '\\') isEscape = true;
            else if (c == '"') break;
            else accumulator.append(c);
        }
        return new Token(TokenType.String, accumulator.toString(), loc);
    }

    public Optional<Token> getNext() throws MacroException {
        if (pos >= content.length()) return Optional.empty();
        while (Character.isWhitespace(peek())) next();
        if (peek() == '/' && peekpeek() == '/') while (true) {
            var c = next();
            if (c == '\n' || c == '\0') break;
        }
        if (peek() == '"') return Optional.of(parseString());
        var fromSingleChar = TokenType.fromSingleCharacter(peek());
        if (fromSingleChar.isPresent()) return Optional.of(new Token(fromSingleChar.get(), "" + next(), loc()));
        if (isNumberCharacter(peek()) || peek() == '.') return Optional.of(parseNumber());
        if (isIdentifierCharacter(peek())) return Optional.of(parseIdentifier());
        throw new MacroException(Translation.UnexpectedChar.with("" + next()), loc());
    }
}
