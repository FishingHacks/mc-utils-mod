package net.fishinghacks.utils.client.calc.parsing;

import net.fishinghacks.utils.client.calc.MathException;
import net.fishinghacks.utils.client.calc.Translation;
import net.fishinghacks.utils.client.calc.exprs.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private final Tokenizer tokenizer;
    @Nullable
    private Token peekedToken = null;

    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Parser(String content) {
        this(new Tokenizer(content));
    }

    public @Nullable Token peek() throws MathException {
        if (peekedToken != null) return peekedToken;
        return peekedToken = tokenizer.getNext().orElse(null);
    }

    public Token next() throws MathException {
        Token token = peek();
        if (token == null) throw new MathException(Translation.ExpectedToken.get(), tokenizer.endPos, "");
        peekedToken = null;
        return token;
    }

    public void expect(TokenType tokenType) throws MathException {
        Token token = peek();
        if (token == null)
            throw new MathException(Translation.ExpectedTokenTypeButNone.with(tokenType.toString()), tokenizer.endPos,
                "");
        peekedToken = null;
        if (token.type() != tokenType)
            throw new MathException(Translation.ExpectedToken.with(tokenType.toString(), token.type().toString()),
                token.charPos(), "");
    }

    public void expect(TokenType tokenType, TokenType... tokens) throws MathException {
        Token token = peek();
        if (token == null) throw new MathException(Translation.ExpectedTokenTypesButNone.with(
            tokenType.toString() + Arrays.stream(tokens).map(v -> ", " + v).reduce("", String::concat)),
            tokenizer.endPos, "");
        peekedToken = null;
        if (token.type() != tokenType) throw new MathException(Translation.ExpectedTokenTypes.with(
            tokenType.toString() + Arrays.stream(tokens).map(v -> ", " + v).reduce("", String::concat),
            token.type().toString()), token.charPos(), "");
    }

    public boolean peekMatch(TokenType... tokens) throws MathException {
        Token token = peek();
        if (token == null) return false;
        for (TokenType type : tokens) if (type == token.type()) return true;
        return false;
    }

    public Expression parseExpectEnd() throws MathException {
        Expression expr = parseExpr();
        Token token = peek();
        if (token == null) return expr;
        throw new MathException(Translation.ExpectedEnd.with(token.type().toString()));
    }

    public Expression parseExpr() throws MathException {
        return parseAddition();
    }

    public Expression parseAddition() throws MathException {
        Expression expr = parseMultiplication();

        while (peekMatch(TokenType.Plus, TokenType.Minus)) expr = switch (next().type()) {
            case TokenType.Minus -> new SubtractionExpr(expr, parseMultiplication());
            case TokenType.Plus -> new AdditionExpr(expr, parseMultiplication());
            default -> throw new MathException(Component.literal("unreachable (addition)"));
        };

        return expr;
    }

    public Expression parseMultiplication() throws MathException {
        Expression expr = parsePower();

        while (peekMatch(TokenType.Multiply, TokenType.Divide, TokenType.Remainder)) expr = switch (next().type()) {
            case TokenType.Multiply -> new MultiplicationExpr(expr, parsePower());
            case TokenType.Divide -> new DivisionExpr(expr, parsePower());
            case TokenType.Remainder -> new RemainderExpr(expr, parsePower());
            default -> throw new MathException(Component.literal("unreachable (multiplication)"));
        };

        return expr;
    }

    public Expression parsePower() throws MathException {
        Expression expr = parseUnary();

        while (peekMatch(TokenType.Power)) {
            next();
            expr = new PowerExpr(expr, parseUnary());
        }

        return expr;
    }

    public Expression parseUnary() throws MathException {
        boolean invert = false;

        while (peekMatch(TokenType.Plus, TokenType.Minus)) if (next().type() == TokenType.Minus) invert = !invert;
        if (invert) return new UnaryMinusExpr(parseCall());
        else return parseCall();
    }

    public Expression parseCall() throws MathException {
        Expression expr = parseLiteral();
        if (expr instanceof VariableExpr(String name, int characterPos) && peekMatch(TokenType.ParenLeft)) {
            next();
            List<Expression> args = new ArrayList<>();
            while (!peekMatch(TokenType.ParenRight)) {
                if (!args.isEmpty()) {
                    expect(TokenType.Comma);
                    if (peekMatch(TokenType.ParenRight)) break;
                }
                args.add(parseExpr());
            }
            next();
            expr = new CallExpr(name, args, characterPos);
        }

        return expr;
    }

    public Expression parseLiteral() throws MathException {
        if (peekMatch(TokenType.ParenLeft)) {
            next();
            Expression expr = parseExpr();
            expect(TokenType.ParenRight);
            return expr;
        }
        if (peekMatch(TokenType.Identifier)) {
            var token = next();
            return new VariableExpr(token.value(), token.charPos());
        }
        if (peekMatch(TokenType.Number)) {
            var token = next();
            try {
                return new LiteralValue(Double.parseDouble(token.value()));
            } catch (NumberFormatException e) {
                throw new MathException(Translation.InvalidNumber.with(e.getMessage()), token.charPos(), "");
            }
        }
        expect(TokenType.ParenRight, TokenType.Identifier, TokenType.Number);
        throw new MathException(Component.keybind("unreachable (literal)"));
    }
}
