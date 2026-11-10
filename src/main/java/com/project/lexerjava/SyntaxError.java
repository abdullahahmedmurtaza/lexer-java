package com.project.lexerjava;

/**
 * Ek syntax error: message + line number.
 * LexError.java jaisi hai, koi JavaFX import nahi.
 */
public class SyntaxError {

    private final int lineNumber;
    private final String message;

    public SyntaxError(int lineNumber, String message) {
        this.lineNumber = lineNumber;
        this.message = message;
    }

    public int getLineNumber() { return lineNumber; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "Line " + lineNumber + ": " + message;
    }
}