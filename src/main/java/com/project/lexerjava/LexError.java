package com.project.lexerjava;

/**
 * Data Model class representing a single lexical error:
 * koi bhi character/sequence jo humari kisi bhi regex pattern se match nahin hua
 * (i.e. C language ka valid token nahin hai).
 */
public class LexError {

    private final String invalidLexeme;
    private final int lineNumber;
    private final String message;

    public LexError(String invalidLexeme, int lineNumber, String message) {
        this.invalidLexeme = invalidLexeme;
        this.lineNumber = lineNumber;
        this.message = message;
    }

    public String getInvalidLexeme() { return invalidLexeme; }
    public int getLineNumber() { return lineNumber; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "Line " + lineNumber + ": " + message + " -> '" + invalidLexeme + "'";
    }
}
