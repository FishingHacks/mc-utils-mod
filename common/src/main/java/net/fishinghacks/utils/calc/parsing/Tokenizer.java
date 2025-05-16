package net.fishinghacks.utils.calc.parsing;

import net.fishinghacks.utils.calc.MathException;
import net.fishinghacks.utils.calc.Translation;

import java.util.Optional;

public class Tokenizer {
    private int pos = 0;
    private final String content;
    public final int endPos;

    public Tokenizer(String content) {
        this.content = content;
        this.endPos = content.length();
    }

    private char peek() {
        return pos < content.length() ? content.charAt(pos) : '\0';
    }

    private char next() {
        return pos < content.length() ? content.charAt(pos++) : '\0';
    }

    private boolean isNumberCharacter(char character) {
        return character >= '0' && character <= '9';
    }

    private boolean isIdentifierCharacter(char character) {
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || (character >= '0' && character <= '9') || character == '_';
    }

    public Token parseNumber() {
        int charPos = pos;
        boolean hasDot = false;
        StringBuilder numberContent = new StringBuilder();
        if (peek() == '.') {
            hasDot = true;
            numberContent.append("0.");
            next();
        } else numberContent.append(next());
        while (isNumberCharacter(peek()) || (!hasDot && peek() == '.')) numberContent.append(next());
        return new Token(TokenType.Number, numberContent.toString(), charPos);
    }

    public Token parseIdentifier() {
        int charPos = pos;
        StringBuilder contents = new StringBuilder();
        while (isIdentifierCharacter(peek())) contents.append(next());
        return new Token(TokenType.Identifier, contents.toString(), charPos);
    }

    public Optional<Token> getNext() throws MathException {
        if (pos >= content.length()) return Optional.empty();
        while (Character.isWhitespace(peek())) next();
        var fromSingleChar = TokenType.fromSingleCharacter(peek());
        if (fromSingleChar.isPresent()) return Optional.of(new Token(fromSingleChar.get(), "" + next(), pos - 1));
        if (isNumberCharacter(peek()) || peek() == '.') return Optional.of(parseNumber());
        if (isIdentifierCharacter(peek())) return Optional.of(parseIdentifier());
        throw new MathException(Translation.UnexpectedChar.with("" + next()), pos - 1, "");
    }
}
