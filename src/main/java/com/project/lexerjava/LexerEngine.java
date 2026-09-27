package com.project.lexerjava;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core Lexer Engine for the C Lexical Analyzer.
 *
 * Responsibility (Clean Architecture - "Engine" layer):
 *   - Read the uploaded .c source file.
 *   - Tokenize it using java.util.regex (Pattern / Matcher) ONLY - no external parser libs.
 *   - Classify every recognized lexeme into a Token Category.
 *   - Collect any unrecognized character/sequence as a LexError with its line number.
 *
 * This class has NO JavaFX imports - it is pure logic, so it can be unit tested
 * independently of the UI (separation of concerns as required by the assignment).
 */
public class LexerEngine {

    // ---------------------------------------------------------------------
    // 1. C LANGUAGE KEYWORDS (C89/C99 standard set)
    // ---------------------------------------------------------------------
    private static final String[] C_KEYWORDS = {
            "auto", "break", "case", "char", "const", "continue", "default", "do",
            "double", "else", "enum", "extern", "float", "for", "goto", "if",
            "int", "long", "register", "return", "short", "signed", "sizeof",
            "static", "struct", "switch", "typedef", "union", "unsigned", "void",
            "volatile", "while", "inline", "restrict", "_Bool", "_Complex", "_Imaginary"
    };

    // ---------------------------------------------------------------------
    // 2. REGEX FRAGMENTS - each fragment matches EXACTLY one token category.
    //    Order matters: more specific / longer alternatives must come BEFORE
    //    more general ones (Java's regex engine picks leftmost alternative
    //    that matches, not the longest one).
    // ---------------------------------------------------------------------

    private static final String WHITESPACE = "[ \\t\\r\\n]+";

    // /* ... */ multi-line comment, non-greedy so it stops at the FIRST closing */.
    private static final String BLOCK_COMMENT = "/\\*[\\s\\S]*?\\*/";

    // // ... single line comment.
    private static final String LINE_COMMENT = "//[^\\n]*";

    // "..." string literal - allows escaped characters inside.
    private static final String STRING_LITERAL = "\"(?:\\\\.|[^\"\\\\\\n])*\"";

    // '...' character literal - e.g. 'a', '\n', '\0'.
    private static final String CHAR_LITERAL = "'(?:\\\\.|[^'\\\\\\n])'";

    // Numeric literals: hex, float, exponent, plain int with optional suffixes.
    private static final String NUMBER_LITERAL =
            "\\b0[xX][0-9a-fA-F]+[uUlL]*\\b"
                    + "|\\b\\d+\\.\\d+(?:[eE][+-]?\\d+)?[fFlL]?\\b"
                    + "|\\b\\d+[eE][+-]?\\d+[fFlL]?\\b"
                    + "|\\b\\d+[uUlL]*\\b";

    // Keywords - \b...\b guarantees whole-word match.
    private static final String KEYWORD_PATTERN =
            "\\b(?:" + String.join("|", C_KEYWORDS) + ")\\b";

    private static final String IDENTIFIER = "[A-Za-z_][A-Za-z0-9_]*";

    // Operators - MULTI-CHARACTER operators MUST be listed before single-char ones.
    private static final String OPERATOR =
            "<<=|>>="
                    + "|==|!=|<=|>=|&&|\\|\\||\\+\\+|--"
                    + "|\\+=|-=|\\*=|/=|%=|&=|\\|=|\\^="
                    + "|->|<<|>>"
                    + "|[+\\-*/%=<>!&|^~]";

    private static final String PUNCTUATION = "[;,\\{\\}\\(\\)\\[\\]\\.:]";

    // Anything else (single character) = invalid/error token.
    private static final String UNKNOWN = ".";

    // ---------------------------------------------------------------------
    // 3. MASTER PATTERN - combines all fragments as named alternatives.
    // ---------------------------------------------------------------------
    private static final Pattern MASTER_PATTERN = Pattern.compile(
            "(?<WHITESPACE>" + WHITESPACE + ")"
                    + "|(?<BLOCKCOMMENT>" + BLOCK_COMMENT + ")"
                    + "|(?<LINECOMMENT>" + LINE_COMMENT + ")"
                    + "|(?<STRING>" + STRING_LITERAL + ")"
                    + "|(?<CHARLIT>" + CHAR_LITERAL + ")"
                    + "|(?<NUMBER>" + NUMBER_LITERAL + ")"
                    + "|(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<IDENTIFIER>" + IDENTIFIER + ")"
                    + "|(?<OPERATOR>" + OPERATOR + ")"
                    + "|(?<PUNCT>" + PUNCTUATION + ")"
                    + "|(?<UNKNOWN>" + UNKNOWN + ")"
    );

    public static class LexResult {
        private final List<Token> tokens;
        private final List<LexError> errors;

        public LexResult(List<Token> tokens, List<LexError> errors) {
            this.tokens = tokens;
            this.errors = errors;
        }

        public List<Token> getTokens() { return tokens; }
        public List<LexError> getErrors() { return errors; }
    }

    /** Reads the .c file line-by-line, joins into a buffer, then tokenizes it. */
    public LexResult analyze(File sourceFile) throws IOException {
        StringBuilder sourceBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (!first) sourceBuilder.append('\n');
                sourceBuilder.append(line);
                first = false;
            }
        }

        return tokenize(sourceBuilder.toString());
    }

    /** Tokenizes raw source text already in memory (also handy for unit tests). */
    public LexResult tokenize(String sourceText) {
        List<Token> tokens = new ArrayList<>();
        List<LexError> errors = new ArrayList<>();

        Matcher matcher = MASTER_PATTERN.matcher(sourceText);

        int currentLine = 1;
        int lastIndex = 0;

        while (matcher.find()) {
            for (int i = lastIndex; i < matcher.start(); i++) {
                if (sourceText.charAt(i) == '\n') currentLine++;
            }
            lastIndex = matcher.start();

            String lexeme = matcher.group();

            if (matcher.group("WHITESPACE") != null) {
                currentLine += countNewlines(sourceText, matcher.start(), matcher.end());
                lastIndex = matcher.end();
                continue;
            } else if (matcher.group("BLOCKCOMMENT") != null) {
                tokens.add(new Token("Comment (Multi-line)", lexeme, currentLine));
                currentLine += countNewlines(sourceText, matcher.start(), matcher.end());
            } else if (matcher.group("LINECOMMENT") != null) {
                tokens.add(new Token("Comment (Single-line)", lexeme, currentLine));
            } else if (matcher.group("STRING") != null) {
                tokens.add(new Token("Literal (String)", lexeme, currentLine));
            } else if (matcher.group("CHARLIT") != null) {
                tokens.add(new Token("Literal (Character)", lexeme, currentLine));
            } else if (matcher.group("NUMBER") != null) {
                tokens.add(new Token("Literal (Number)", lexeme, currentLine));
            } else if (matcher.group("KEYWORD") != null) {
                tokens.add(new Token("Keyword", lexeme, currentLine));
            } else if (matcher.group("IDENTIFIER") != null) {
                tokens.add(new Token("Identifier", lexeme, currentLine));
            } else if (matcher.group("OPERATOR") != null) {
                tokens.add(new Token("Operator (" + classifyOperator(lexeme) + ")", lexeme, currentLine));
            } else if (matcher.group("PUNCT") != null) {
                tokens.add(new Token("Punctuation", lexeme, currentLine));
            } else if (matcher.group("UNKNOWN") != null) {
                if (!lexeme.trim().isEmpty()) {
                    errors.add(new LexError(lexeme, currentLine, "Unrecognized character/token"));
                }
            }

            lastIndex = matcher.end();
        }

        return new LexResult(tokens, errors);
    }

    private int countNewlines(String sourceText, int from, int to) {
        int count = 0;
        for (int i = from; i < to; i++) {
            if (sourceText.charAt(i) == '\n') count++;
        }
        return count;
    }

    /** Sub-classifies an OPERATOR lexeme: Arithmetic / Relational / Logical / Assignment / Bitwise. */
    private String classifyOperator(String op) {
        switch (op) {
            case "+": case "-": case "*": case "/": case "%": case "++": case "--":
                return "Arithmetic";
            case "==": case "!=": case "<": case ">": case "<=": case ">=":
                return "Relational";
            case "&&": case "||": case "!":
                return "Logical";
            case "=": case "+=": case "-=": case "*=": case "/=": case "%=":
            case "&=": case "|=": case "^=": case "<<=": case ">>=":
                return "Assignment";
            case "&": case "|": case "^": case "~": case "<<": case ">>":
                return "Bitwise";
            default:
                return "Other";
        }
    }
}
