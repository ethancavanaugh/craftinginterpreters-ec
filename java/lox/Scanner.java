package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!atEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            //Single char tokens
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            //Operators (one or two char)
            case '!':
                addToken(nextMatches('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(nextMatches('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(nextMatches('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(nextMatches('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (nextMatches('/')) {
                    // A comment goes until the end of the line
                    while (peek() != '\n' && !atEnd()) advance();
                }
                else {
                    addToken(SLASH);
                }
                break;

            //Whitespace
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;

            //String literals
            case '"': scanString(); break;

            default:
                //Number literals (handled by default to reduce cases)
                if (isDigit(c)) {
                    scanNumber();
                }
                else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void scanString() {
        while (peek() != '"' && !atEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (atEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();
        //Trim quotation marks
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void scanNumber() {
        while (isDigit(peek())) advance();
        //Include decimal part if exists
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        if (atEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean nextMatches(char expected) {
        if (atEnd() || source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean atEnd() {
        return current >= source.length();
    }
}