package net.fishinghacks.utils.macros.parsing;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.macros.FunctionValue;
import net.fishinghacks.utils.macros.MacroException;
import net.fishinghacks.utils.macros.Translation;
import net.fishinghacks.utils.macros.exprs.*;
import net.fishinghacks.utils.macros.statements.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

public class Parser {
    private final Tokenizer tokenizer;
    @Nullable
    private Token peekedToken = null;
    private boolean isInLoop = false;
    private boolean isInFunction = false;

    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Parser(String content, String file) {
        this(new Tokenizer(content, file));
    }

    public @Nullable Token peek() throws MacroException {
        if (peekedToken != null) return peekedToken;
        peekedToken = tokenizer.getNext().orElse(null);
        if (peekedToken != null) Constants.LOG.info("Got {} ({})", peekedToken.type(), peekedToken.value());
        else Constants.LOG.info("Got no token (Position : {})", tokenizer.pos);
        return peekedToken;
    }

    public boolean atEnd() throws MacroException {
        return peek() == null;
    }

    public Token next() throws MacroException {
        Token token = peekExpectAnyToken();
        peekedToken = null;
        return token;
    }

    public Token peekExpectAnyToken() throws MacroException {
        Token token = peek();
        if (token == null) throw new MacroException(Translation.ExpectedToken.get(), tokenizer.endLoc);
        return token;
    }

    public Token expect(TokenType tokenType) throws MacroException {
        Token token = peekExpectAnyToken();
        peekedToken = null;
        if (token.type() != tokenType)
            throw new MacroException(Translation.ExpectedTokenType.with(tokenType.toString(), token.type().toString()),
                token.location());
        return token;
    }

    public void expect(TokenType tokenType, TokenType... tokens) throws MacroException {
        Token token = peek();
        if (token == null) throw new MacroException(Translation.ExpectedTokenTypesButNone.with(
            tokenType.toString() + Arrays.stream(tokens).map(v -> ", " + v).reduce("", String::concat)),
            tokenizer.endLoc);
        peekedToken = null;
        if (token.type() == tokenType) return;
        for (var expectedTokenType : tokens) if (token.type() == expectedTokenType) return;
        throw new MacroException(Translation.ExpectedTokenTypes.with(
            tokenType.toString() + Arrays.stream(tokens).map(v -> ", " + v).reduce("", String::concat),
            token.type().toString()), token.location());
    }

    public boolean peekMatch(TokenType... tokens) throws MacroException {
        Token token = peek();
        if (token == null) return false;
        for (TokenType type : tokens) if (type == token.type()) return true;
        return false;
    }

    public Optional<Statement> parseStatement() throws MacroException {
        while (peekMatch(TokenType.Semicolon)) next();
        Token token = peek();
        if (token == null) return Optional.empty();
        TokenType type = token.type();
        var stmt = switch (type) {
            case KeywordBreak -> {
                next();
                expect(TokenType.Semicolon);
                if (!isInLoop) throw new MacroException(Translation.LoopKwInvalid.get(), next().location());
                yield new BreakStatement();
            }
            case KeywordContinue -> {
                next();
                expect(TokenType.Semicolon);
                if (!isInLoop) throw new MacroException(Translation.LoopKwInvalid.get(), next().location());
                yield new ContinueStatement();
            }
            case KeywordReturn -> {
                var loc = next().location();
                var expr = peekMatch(TokenType.Semicolon) ? null : parseExpr();
                expect(TokenType.Semicolon);
                if (!isInFunction) throw new MacroException(Translation.ReturnKwInvalid.get(), loc);
                yield new ReturnStatement(expr);
            }
            case KeywordFor -> {
                var location = next().location();
                expect(TokenType.ParenOpen);
                var name = expect(TokenType.Identifier).value();
                expect(TokenType.KeywordIn);
                var expr = parseExpr();
                expect(TokenType.ParenClose);
                var body = parseStatementList();
                yield new ForStatement(expr, body, name, location);
            }
            case KeywordWhile -> {
                next();
                expect(TokenType.ParenOpen);
                var expr = parseExpr();
                expect(TokenType.ParenClose);
                var body = parseStatementList();
                yield new WhileStatement(expr, body);
            }
            case KeywordLet -> {
                next();
                var name = expect(TokenType.Identifier).value();
                expect(TokenType.Equal);
                var expr = parseExpr();
                expect(TokenType.Semicolon);
                yield new VariableDefinitionStatement(name, expr);
            }
            case KeywordIf -> {
                next();
                var cases = new ArrayList<IfStatement.IfEntry>();
                expect(TokenType.ParenOpen);
                var ifExpr = parseExpr();
                expect(TokenType.ParenClose);
                cases.add(new IfStatement.IfEntry(ifExpr, parseStatementList()));
                List<Statement> elseBody = null;
                while (peekMatch(TokenType.KeywordElse)) {
                    next();
                    if (!peekMatch(TokenType.KeywordIf)) {
                        elseBody = parseStatementList();
                        break;
                    }
                    expect(TokenType.ParenOpen);
                    var expr = parseExpr();
                    expect(TokenType.ParenClose);
                    cases.add(new IfStatement.IfEntry(expr, parseStatementList()));
                }
                yield new IfStatement(elseBody, cases);
            }
            case KeywordFunction -> {
                boolean initialInFunction = isInFunction;
                boolean initialInLoop = isInLoop;
                isInFunction = false;
                isInLoop = false;
                try {
                    next();
                    var name = expect(TokenType.Identifier).value();
                    expect(TokenType.ParenOpen);
                    var args = new ArrayList<String>();
                    while (!peekMatch(TokenType.ParenClose)) {
                        if (!args.isEmpty()) {
                            expect(TokenType.Comma);
                            if (peekMatch(TokenType.ParenClose)) break;
                        }
                        args.add(expect(TokenType.Identifier).value());
                    }
                    next();
                    List<Statement> body;
                    isInFunction = true;
                    if (peekMatch(TokenType.Equal)) {
                        next();
                        body = List.of(new ReturnStatement(parseExpr()));
                        expect(TokenType.Semicolon);
                    } else body = parseStatementList();
                    isInFunction = initialInFunction;
                    isInLoop = initialInLoop;
                    yield new FunctionStatement(name, new FunctionValue(name, args, body));
                } finally {
                    isInFunction = initialInFunction;
                    isInLoop = initialInLoop;
                }
            }
            default -> {
                var expr = parseExpr();
                expect(TokenType.Semicolon);
                yield new ExprStatement(expr);
            }
        };
        while (peekMatch(TokenType.Semicolon)) next();
        return Optional.of(stmt);
    }

    private List<Statement> parseStatementList() throws MacroException {
        if (peekMatch(TokenType.CurlyOpen)) {
            next();
            var list = new ArrayList<Statement>();
            while (!peekMatch(TokenType.CurlyClose)) {
                peekExpectAnyToken();
                var statement = parseStatement();
                if (statement.isEmpty()) throw new IllegalStateException();
                list.add(statement.get());
            }
            next();
            return list;
        }
        peekExpectAnyToken();
        var statement = parseStatement();
        if (statement.isEmpty()) throw new IllegalStateException();
        return List.of(statement.get());
    }

    public Expression parseExpr() throws MacroException {
        return parseInlineIf();
    }

    public Expression parseInlineIf() throws MacroException {
        Expression expr = parseBoolOp();
        while (peekMatch(TokenType.QuestionMark)) {
            next();
            var left = parseExpr();
            expect(TokenType.Colon);
            expr = new InlineIfExpr(expr, left, parseExpr());
        }
        return expr;
    }

    public Expression parseBoolOp() throws MacroException {
        Expression expr = parseComparison();
        while (peekMatch(TokenType.Ampersand, TokenType.Pipe)) {
            var type = next().type();
            expect(type);
            var right = parseComparison();
            if (type == TokenType.Ampersand) expr = new AndExpr(expr, right);
            else expr = new OrExpr(expr, right);
        }
        return expr;
    }

    public Expression parseComparison() throws MacroException {
        Expression expr = parseAddition();
        while (peekMatch(TokenType.LessThan, TokenType.GreaterThan, TokenType.Equal, TokenType.ExclamationMark)) {
            BiFunction<Expression, Expression, Expression> constructor;
            switch (next().type()) {
                case Equal -> {
                    expect(TokenType.Equal);
                    constructor = EqExpr::new;
                }
                case ExclamationMark -> {
                    expect(TokenType.Equal);
                    constructor = NotEqExpr::new;
                }
                case LessThan -> {
                    if (peekMatch(TokenType.Equal)) {
                        next();
                        constructor = LessThanEqExpr::new;
                    } else constructor = LessThanExpr::new;
                }
                case GreaterThan -> {
                    if (peekMatch(TokenType.Equal)) {
                        next();
                        constructor = GreaterThanEqExpr::new;
                    } else constructor = GreaterThanExpr::new;
                }
                default -> throw new IllegalStateException();
            }
            expr = constructor.apply(expr, parseAddition());
        }
        return expr;
    }

    public Expression parseAddition() throws MacroException {
        Expression expr = parseMultiplication();

        while (peekMatch(TokenType.Plus, TokenType.Minus)) expr = switch (next().type()) {
            case TokenType.Minus -> new SubtractionExpr(expr, parseMultiplication());
            case TokenType.Plus -> new AdditionExpr(expr, parseMultiplication());
            default -> throw new IllegalStateException();
        };

        return expr;
    }

    public Expression parseMultiplication() throws MacroException {
        Expression expr = parsePower();

        while (peekMatch(TokenType.Multiply, TokenType.Divide, TokenType.Remainder)) expr = switch (next().type()) {
            case TokenType.Multiply -> new MultiplicationExpr(expr, parsePower());
            case TokenType.Divide -> new DivisionExpr(expr, parsePower());
            case TokenType.Remainder -> new RemainderExpr(expr, parsePower());
            default -> throw new IllegalStateException();
        };

        return expr;
    }

    public Expression parsePower() throws MacroException {
        Expression expr = parseUnary();

        while (peekMatch(TokenType.Power)) {
            next();
            expr = new PowerExpr(expr, parseUnary());
        }

        return expr;
    }

    public Expression parseUnary() throws MacroException {
        while (peekMatch(TokenType.Plus, TokenType.Minus, TokenType.ExclamationMark)) {
            switch (next().type()) {
                case Plus -> {
                }
                case Minus -> {
                    if (peekMatch(TokenType.Minus)) next();
                    else return new UnaryMinusExpr(parseUnary());
                }
                case ExclamationMark -> {
                    if (peekMatch(TokenType.ExclamationMark)) next();
                    else return new NotExpr(parseUnary());
                }
            }
        }
        return parseCall();
    }

    public Expression parseCall() throws MacroException {
        Expression expr = parseIndex();
        while (peekMatch(TokenType.ParenOpen)) {
            var loc = next().location();
            List<Expression> args = new ArrayList<>();
            while (!peekMatch(TokenType.ParenClose)) {
                if (!args.isEmpty()) {
                    expect(TokenType.Comma);
                    if (peekMatch(TokenType.ParenClose)) break;
                }
                args.add(parseExpr());
            }
            next();
            expr = new CallExpr(expr, args, loc);
        }

        return expr;
    }

    public Expression parseIndex() throws MacroException {
        Expression expr = parseLiteral();
        while (peekMatch(TokenType.Dot, TokenType.BracketOpen)) {
            if (next().type() == TokenType.Dot) {
                if (!peekMatch(TokenType.Identifier)) expect(TokenType.Identifier);
                var next = next();
                expr = new IndexExpr(expr, new LiteralValue(next.value()), next.location());
            } else {
                var nextTok = peek();
                var right = parseExpr();
                expect(TokenType.BracketClose);
                assert nextTok != null;
                expr = new IndexExpr(expr, right, nextTok.location());
            }
        }
        return expr;
    }

    public Expression parseLiteral() throws MacroException {
        if (peekMatch(TokenType.ParenOpen)) {
            next();
            Expression expr = parseExpr();
            expect(TokenType.ParenClose);
            return expr;
        }
        if (peekMatch(TokenType.Identifier)) {
            var token = next();
            return new VariableExpr(token.value(), token.location());
        }
        if (peekMatch(TokenType.Number)) {
            var token = next();
            try {
                return new LiteralValue(Double.parseDouble(token.value()));
            } catch (NumberFormatException e) {
                throw new MacroException(Translation.InvalidNumber.with(e.getMessage()), token.location());
            }
        }
        if (peekMatch(TokenType.String)) return new LiteralValue(next().value());
        if (peekMatch(TokenType.Pipe)) {
            next();
            var args = new ArrayList<String>();
            while (!peekMatch(TokenType.Pipe)) {
                if (!args.isEmpty()) {
                    expect(TokenType.Comma);
                    if (peekMatch(TokenType.Pipe)) break;
                }
                args.add(expect(TokenType.Identifier).value());
            }
            next();
            List<Statement> body;
            if (peekMatch(TokenType.CurlyOpen)) body = parseStatementList();
            else body = List.of(new ReturnStatement(parseExpr()));
            return new LiteralValue(new FunctionValue("<anonymous>", args, body));
        }
        if (peekMatch(TokenType.BracketOpen)) {
            next();
            var values = new ArrayList<Expression>();
            while (!peekMatch(TokenType.BracketClose)) {
                if (!values.isEmpty()) {
                    expect(TokenType.Comma);
                    if (peekMatch(TokenType.BracketClose)) break;
                }
                values.add(parseExpr());
            }
            next();
            return new ArrayExpression(values);
        }

        if (peekMatch(TokenType.CurlyOpen)) {
            next();
            var values = new HashMap<String, Expression>();
            while (!peekMatch(TokenType.CurlyClose)) {
                if (!values.isEmpty()) {
                    expect(TokenType.Comma);
                    if (peekMatch(TokenType.CurlyClose)) break;
                }
                var key = expect(TokenType.Identifier).value();
                expect(TokenType.Colon);
                values.put(key, parseExpr());
            }
            next();
            return new ObjectExpression(values);
        }
        expect(TokenType.ParenOpen, TokenType.Identifier, TokenType.Number, TokenType.String, TokenType.Pipe,
            TokenType.BracketOpen, TokenType.CurlyOpen);
        throw new IllegalStateException();
    }
}
