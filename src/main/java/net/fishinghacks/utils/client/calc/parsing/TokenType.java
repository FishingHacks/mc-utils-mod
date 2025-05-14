package net.fishinghacks.utils.client.calc.parsing;

import java.util.Optional;

public enum TokenType {
    Plus, Minus, Multiply, Divide, Remainder, Number, Power, Identifier, ParenLeft, ParenRight, Comma;

    public static Optional<TokenType> fromSingleCharacter(char characer) {
        return Optional.ofNullable(switch (characer) {
            case '+' -> TokenType.Plus;
            case '-' -> TokenType.Minus;
            case '*' -> TokenType.Multiply;
            case '/' -> TokenType.Divide;
            case '%' -> TokenType.Remainder;
            case '^' -> TokenType.Power;
            case '(' -> TokenType.ParenLeft;
            case ')' -> TokenType.ParenRight;
            case ',' -> TokenType.Comma;
            default -> null;
        });
    }

    public String toString() {
        return switch (this) {
            case Plus -> "+";
            case Minus -> "-";
            case Multiply -> "*";
            case Divide -> "/";
            case Remainder -> "%";
            case Number -> "number";
            case Power -> "^";
            case Identifier -> "identifier";
            case ParenLeft -> "(";
            case ParenRight -> ")";
            case Comma -> ",";
        };
    }
}
