package net.fishinghacks.utils.macros.parsing;

import java.util.Optional;

public enum TokenType {
    Plus, Minus, Multiply, Divide, Remainder, Number, Power, Identifier, String, ParenOpen, ParenClose, Comma,
    Semicolon, KeywordIf, KeywordElse, KeywordWhile, KeywordFunction, KeywordReturn, KeywordFor, KeywordIn,
    KeywordBreak, KeywordContinue, KeywordLet, KeywordNull, CurlyOpen, CurlyClose, BracketOpen, BracketClose,
    QuestionMark, Colon, Pipe, Ampersand, ExclamationMark, LessThan, GreaterThan, Equal, Dot;

    public static Optional<TokenType> fromSingleCharacter(char character) {
        return Optional.ofNullable(switch (character) {
            case '+' -> Plus;
            case '-' -> Minus;
            case '*' -> Multiply;
            case '/' -> Divide;
            case '%' -> Remainder;
            case '^' -> Power;
            case '(' -> ParenOpen;
            case ')' -> ParenClose;
            case ',' -> Comma;
            case ';' -> Semicolon;
            case '{' -> CurlyOpen;
            case '}' -> CurlyClose;
            case '[' -> BracketOpen;
            case ']' -> BracketClose;
            case '?' -> QuestionMark;
            case ':' -> Colon;
            case '|' -> Pipe;
            case '&' -> Ampersand;
            case '!' -> ExclamationMark;
            case '<' -> LessThan;
            case '>' -> GreaterThan;
            case '=' -> Equal;
            case '.' -> Dot;
            default -> null;
        });
    }

    public static Optional<TokenType> fromKeyword(String keyword) {
        return Optional.ofNullable(switch (keyword) {
            case "if" -> KeywordIf;
            case "else" -> KeywordElse;
            case "while" -> KeywordWhile;
            case "fn" -> KeywordFunction;
            case "return" -> KeywordReturn;
            case "for" -> KeywordFor;
            case "in" -> KeywordIn;
            case "break" -> KeywordBreak;
            case "continue" -> KeywordContinue;
            case "let" -> KeywordLet;
            case "null" -> KeywordNull;
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
            case String -> "string";
            case ParenOpen -> "(";
            case ParenClose -> ")";
            case Comma -> ",";
            case Semicolon -> ";";
            case KeywordIf -> "if";
            case KeywordElse -> "else";
            case KeywordWhile -> "while";
            case KeywordFunction -> "fn";
            case KeywordReturn -> "return";
            case KeywordFor -> "for";
            case KeywordIn -> "in";
            case KeywordBreak -> "break";
            case KeywordContinue -> "continue";
            case KeywordLet -> "let";
            case KeywordNull -> "null";
            case CurlyOpen -> "{";
            case CurlyClose -> "}";
            case BracketOpen -> "[";
            case BracketClose -> "]";
            case QuestionMark -> "?";
            case Colon -> ":";
            case Pipe -> "|";
            case Ampersand -> "&";
            case ExclamationMark -> "!";
            case LessThan -> "<";
            case GreaterThan -> ">";
            case Equal -> "=";
            case Dot -> ".";
        };
    }
}
