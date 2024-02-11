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

            default:
                Lox.error(line, "Unexpected character.");
                break;
        }
    }

    private char advance() {
        return source.charAt(current++);
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

    private char peek() {
        if (atEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean atEnd() {
        return current >= source.length();
    }
}