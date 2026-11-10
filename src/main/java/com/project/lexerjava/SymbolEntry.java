package com.project.lexerjava;

import javafx.beans.property.SimpleStringProperty;

/**
 * Symbol table ki ek row: Lexeme | Type | Scope | Array size.
 * Token.java ki tarah JavaFX properties use ki hain taake TableView mein bind ho sake.
 */
public class SymbolEntry {

    private final SimpleStringProperty lexeme;
    private final SimpleStringProperty type;
    private final SimpleStringProperty scope;
    private final SimpleStringProperty arraySize; // "" agar array nahi hai

    public SymbolEntry(String lexeme, String type, String scope, String arraySize) {
        this.lexeme = new SimpleStringProperty(lexeme);
        this.type = new SimpleStringProperty(type);
        this.scope = new SimpleStringProperty(scope);
        this.arraySize = new SimpleStringProperty(arraySize);
    }

    public String getLexeme() { return lexeme.get(); }
    public void setLexeme(String lexeme) { this.lexeme.set(lexeme); }
    public SimpleStringProperty lexemeProperty() { return lexeme; }

    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public SimpleStringProperty typeProperty() { return type; }

    public String getScope() { return scope.get(); }
    public void setScope(String scope) { this.scope.set(scope); }
    public SimpleStringProperty scopeProperty() { return scope; }

    public String getArraySize() { return arraySize.get(); }
    public void setArraySize(String arraySize) { this.arraySize.set(arraySize); }
    public SimpleStringProperty arraySizeProperty() { return arraySize; }

    @Override
    public String toString() {
        return getLexeme() + "\t" + getType() + "\t" + getScope() + "\t" + getArraySize();
    }
}