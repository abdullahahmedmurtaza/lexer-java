package com.project.lexerjava;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Data Model class representing a single recognized token produced by the LexerEngine.
 *
 * Har token ke 3 fields hain jo assignment ne mangi hain:
 *  1. category   -> Token Category  (e.g. "Keyword", "Identifier", "Operator (Arithmetic)")
 *  2. lexeme     -> actual matched string from source code (e.g. "int", "x", "+")
 *  3. lineNumber -> line number (1-based) jahan ye token mila
 *
 * JavaFX ki TableView ko PropertyValueFactory ke through columns bind karne ke liye
 * "bean style" properties chahiye hoti hain (categoryProperty(), lexemeProperty(), lineNumberProperty()).
 * Isliye plain fields ki bajaye SimpleStringProperty / SimpleIntegerProperty use ki hain.
 */
public class Token {

    private final SimpleStringProperty category;
    private final SimpleStringProperty lexeme;
    private final SimpleIntegerProperty lineNumber;

    public Token(String category, String lexeme, int lineNumber) {
        this.category = new SimpleStringProperty(category);
        this.lexeme = new SimpleStringProperty(lexeme);
        this.lineNumber = new SimpleIntegerProperty(lineNumber);
    }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }
    public SimpleStringProperty categoryProperty() { return category; }

    public String getLexeme() { return lexeme.get(); }
    public void setLexeme(String lexeme) { this.lexeme.set(lexeme); }
    public SimpleStringProperty lexemeProperty() { return lexeme; }

    public int getLineNumber() { return lineNumber.get(); }
    public void setLineNumber(int lineNumber) { this.lineNumber.set(lineNumber); }
    public SimpleIntegerProperty lineNumberProperty() { return lineNumber; }

    @Override
    public String toString() {
        return "Token{category='" + getCategory() + "', lexeme='" + getLexeme() + "', line=" + getLineNumber() + "}";
    }
}


