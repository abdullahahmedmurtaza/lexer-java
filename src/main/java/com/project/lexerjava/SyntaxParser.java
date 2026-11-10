package com.project.lexerjava;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Phase 2: Syntax Analyzer (top-down / recursive descent, single pass).
 * Input : LexerEngine ke tokens.
 * Output: ParseResult (symbol table + syntax errors).
 * Is file mein koi JavaFX import nahi.
 */
public class SyntaxParser {

    // ------------------------------------------------------------ result
    public static class ParseResult {
        private final List<SymbolEntry> symbols;
        private final List<SyntaxError> errors;

        public ParseResult(List<SymbolEntry> symbols, List<SyntaxError> errors) {
            this.symbols = symbols;
            this.errors = errors;
        }

        public List<SymbolEntry> getSymbols() { return symbols; }
        public List<SyntaxError> getErrors() { return errors; }
    }

    // ------------------------------------------------------------ internal types
    private enum Kind { KEYWORD, ID, INT, CHAR, OTHER_LITERAL, OP, PUNCT, EOF }

    /** Parser ke liye Token ka halka phulka version. */
    private static final class Tok {
        final Kind kind;
        final String lexeme;
        final int line;

        Tok(Kind kind, String lexeme, int line) {
            this.kind = kind;
            this.lexeme = lexeme;
            this.line = line;
        }
    }

    /** Syntax error milne par throw hota hai, statement level par catch hota hai. */
    private static final class SyntaxException extends RuntimeException {
        final int line;

        SyntaxException(int line, String message) {
            super(message, null, false, false);
            this.line = line;
        }
    }

    private static final Set<String> TYPES = Set.of("void", "char", "int");
    private static final Set<String> OPERATORS =
            Set.of("+", "-", "*", "/", ">", "<", "=", "==", "<=", ">=", "!=");

    // ------------------------------------------------------------ state
    private List<Tok> toks;
    private int pos;
    private List<SymbolEntry> symbols;
    private List<SyntaxError> errors;
    private String scope;

    // Error recovery ke liye brace ginti
    private int pendingClose; // skip hue '}' jinhon ne ek khula block pehle hi band kar diya
    private int pendingOpen;  // skip hue '{' jinke '}' ko baad mein ignore karna hai
    // ------------------------------------------------------------ entry point
    public ParseResult parse(List<Token> lexerTokens) {
        toks = convert(lexerTokens);
        pos = 0;
        symbols = new ArrayList<>();
        errors = new ArrayList<>();
        scope = "Global";

        while (!atEnd()) {
            int start = pos;
            try {
                external();
            } catch (SyntaxException e) {
                report(e);
                recoverTopLevel(e.line);
            }
            scope = "Global";
            pendingClose = 0;
            pendingOpen = 0;
            if (pos == start && !atEnd()) pos++; // safety: parser kabhi atakna nahi chahiye
        }
        return new ParseResult(symbols, errors);
    }

    /** Lexer Token -> Tok. Comments yahin nikal diye jate hain. */
    private List<Tok> convert(List<Token> lexerTokens) {
        List<Tok> out = new ArrayList<>();
        int lastLine = 1;
        for (Token t : lexerTokens) {
            String cat = t.getCategory();
            String lex = t.getLexeme();
            int line = t.getLineNumber();
            lastLine = line;

            if (cat.startsWith("Comment")) continue;

            if (cat.equals("Keyword")) {
                out.add(new Tok(Kind.KEYWORD, lex, line));
            } else if (cat.equals("Identifier")) {
                out.add(new Tok(Kind.ID, lex, line));
            } else if (cat.equals("Literal (Number)")) {
                boolean isInt = lex.matches("(?i)0x[0-9a-f]+[ul]*|\\d+[ul]*");
                out.add(new Tok(isInt ? Kind.INT : Kind.OTHER_LITERAL, lex, line));
            } else if (cat.equals("Literal (Character)")) {
                out.add(new Tok(Kind.CHAR, lex, line));
            } else if (cat.startsWith("Literal")) {
                out.add(new Tok(Kind.OTHER_LITERAL, lex, line));
            } else if (cat.startsWith("Operator")) {
                out.add(new Tok(Kind.OP, lex, line));
            } else if (cat.equals("Punctuation")) {
                out.add(new Tok(Kind.PUNCT, lex, line));
            }
        }
        // End-of-file marker, taake kabhi index out of range na ho
        int eofLine = out.isEmpty() ? lastLine : out.get(out.size() - 1).line;
        out.add(new Tok(Kind.EOF, "<EOF>", eofLine));
        return out;
    }

    // ------------------------------------------------------------ token helpers
    private Tok cur() { return toks.get(pos); }

    private boolean atEnd() { return cur().kind == Kind.EOF; }

    /** Current token wapas karo aur aage barho (EOF se aage nahi jata). */
    private Tok next() {
        Tok t = cur();
        if (t.kind != Kind.EOF) pos++;
        return t;
    }

    private boolean isP(String s) { return cur().kind == Kind.PUNCT && cur().lexeme.equals(s); }
    private boolean isKw(String s) { return cur().kind == Kind.KEYWORD && cur().lexeme.equals(s); }
    private boolean isType() { return cur().kind == Kind.KEYWORD && TYPES.contains(cur().lexeme); }
    private boolean isId() { return cur().kind == Kind.ID; }
    private boolean isOperator() { return cur().kind == Kind.OP && OPERATORS.contains(cur().lexeme); }
    private boolean isConstant() { return cur().kind == Kind.INT || cur().kind == Kind.CHAR; }

    // ------------------------------------------------------------ error helpers
    /**
     * "X expected" errors: agar current token agli line par hai, to cheez pichli line ke
     * end par missing thi, isliye pichli line ka number report hota hai.
     */
    private SyntaxException expected(String message) {
        int line = cur().line;
        if (pos > 0 && toks.get(pos - 1).line < line) line = toks.get(pos - 1).line;
        return new SyntaxException(line, message);
    }

    /** Aise errors jahan current token hi galat hai: uski apni line. */
    private SyntaxException unexpected(String message) {
        return new SyntaxException(cur().line, message);
    }

    private void report(SyntaxException e) {
        errors.add(new SyntaxError(e.line, e.getMessage()));
    }

    /** Punctuation match karo, warna error. */
    private void expectP(String punct, String message) {
        if (!isP(punct)) throw expected(message);
        next();
    }

    private void expectSemicolon() { expectP(";", "Semicolon expected"); }

    private Tok expectId(String message) {
        if (!isId()) throw expected(message);
        return next();
    }

    /** Error wali line ke baaki saare tokens skip karo (assignment ki requirement). */
    private void skipLine(int errorLine) {
        while (!atEnd() && cur().line <= errorLine) next();
    }
    /**
     * Block ke andar error: error wali line ke baaki tokens skip karo.
     * Skip hue braces yaad rakhte hain taake block ka structure na bigde.
     */
    private void recover(int errorLine) {
        int depth = 0;
        while (!atEnd() && cur().line <= errorLine) {
            if (isP("{")) depth++;
            else if (isP("}")) {
                if (depth > 0) depth--;
                else pendingClose++;
            }
            next();
        }
        pendingOpen += depth;
    }

    /** Block ke bahar error: line skip karo, aur skip hue '{' ki poori body chup-chaap skip karo. */
    private void recoverTopLevel(int errorLine) {
        int depth = 0;
        while (!atEnd() && cur().line <= errorLine) {
            if (isP("{")) depth++;
            else if (isP("}") && depth > 0) depth--;
            next();
        }
        while (!atEnd() && depth > 0) {
            if (isP("{")) depth++;
            else if (isP("}")) depth--;
            next();
        }
    }

    // ------------------------------------------------------------ symbol table
    private void addSymbol(String lexeme, String type, String sc, String size) {
        symbols.add(new SymbolEntry(lexeme, type, sc, size));
    }

    // ------------------------------------------------------------ grammar

    /** external -> type ID ( '(' params ')' block | decl_rest ';' ) */
    private void external() {
        if (!isType()) throw unexpected("Type specifier (void, char or int) expected");
        Tok type = next();
        Tok id = expectId("Identifier expected");

        if (isP("(")) {
            // Function: naam symbol table mein, scope Global
            addSymbol(id.lexeme, "Function", "Global", "");
            next(); // (
            String saved = scope;
            scope = id.lexeme; // ab parameters aur body ka scope function ka naam
            try {
                params();
                expectP(")", "Closing parenthesis ')' expected");
                block();
            } finally {
                scope = saved;
            }
        } else {
            // Global variable(s): char a, A, b, b;
            declRest(type.lexeme, id);
            expectSemicolon();
        }
    }

    /** params -> ε | param (',' param)*     param -> type [ID array_opt] */
    private void params() {
        if (isP(")")) return; // main()
        while (true) {
            if (!isType()) throw expected("Type specifier (void, char or int) expected");
            Tok type = next();
            if (isId()) {                       // (char d)   (void) mein naam nahi hota
                Tok id = next();
                String size = arrayOpt();
                addSymbol(id.lexeme, type.lexeme, scope, size);
            }
            if (isP(",")) next();
            else break;
        }
    }

    /** decl_rest -> declarator (',' ID declarator)* ; pehla ID pehle hi consume ho chuka hai */
    private void declRest(String type, Tok firstId) {
        declarator(type, firstId);
        while (isP(",")) {
            next();
            Tok id = expectId("Identifier expected");
            declarator(type, id);
        }
    }

    /** declarator -> array_opt init_opt ; yahan symbol table mein row add hoti hai */
    private void declarator(String type, Tok id) {
        String size = arrayOpt();
        initOpt();
        addSymbol(id.lexeme, type, scope, size);
    }

    /** array_opt -> '[' [INT] ']' | ε   (e[10] -> "10", g[] -> "0", array nahi -> "") */
    private String arrayOpt() {
        // "a 10]" ya "a ]" : '[' missing hai
        if (isP("]") || cur().kind == Kind.INT) throw expected("Left bracket '[' expected");
        if (!isP("[")) return "";
        next(); // [
        String size = "0";
        if (cur().kind == Kind.INT) {
            size = intValue(next().lexeme);
        } else if (!isP("]")) {
            throw expected("Integer constant expected");
        }
        expectP("]", "Right bracket ']' expected");
        return size;
    }

    private String intValue(String lexeme) {
        String s = lexeme.replaceAll("(?i)[ul]+$", "");
        try {
            if (s.startsWith("0x") || s.startsWith("0X")) return String.valueOf(Long.parseLong(s.substring(2), 16));
            return String.valueOf(Long.parseLong(s));
        } catch (NumberFormatException e) {
            return s;
        }
    }

    /** init_opt -> '=' constant | ε */
    private void initOpt() {
        if (cur().kind == Kind.OP && cur().lexeme.equals("=")) {
            next();
            constant();
        }
    }

    private void constant() {
        if (!isConstant()) throw expected("Integer or character constant expected");
        next();
    }


    /** block -> '{' statement* '}' */
    private void block() {
        expectP("{", "Opening brace '{' expected");
        statementList();
        closeBrace();
    }

    private void closeBrace() {
        if (pendingClose > 0) { // is block ka '}' recovery pehle hi kha chuki hai
            pendingClose--;
            return;
        }
        expectP("}", "Closing brace '}' expected");
    }

    private void statementList() {
        while (true) {
            if (pendingClose > 0 || atEnd()) return;
            if (isP("}")) {
                if (pendingOpen > 0) { // skip hue '{' ka '}' : ignore
                    next();
                    pendingOpen--;
                    continue;
                }
                return;
            }
            int start = pos;
            try {
                statement();
            } catch (SyntaxException e) {
                report(e);
                recover(e.line);
                if (pos == start && !atEnd()) pos++; // safety
            }
        }
    }

    private void statement() {
        Tok t = cur();
        if (isP("{")) {
            block();
        } else if (isP(";")) {
            next();
        } else if (t.kind == Kind.KEYWORD) {
            switch (t.lexeme) {
                case "void", "char", "int" -> declaration();
                case "if" -> ifStmt();
                case "while" -> whileStmt();
                case "do" -> doStmt();
                case "for" -> forStmt();
                case "else" -> throw unexpected("'else' without a matching 'if'");
                default -> throw unexpected("Keyword '" + t.lexeme
                        + "' is not supported (type specifier, if, while, do or for expected)");
            }
        } else if (t.kind == Kind.ID) {
            expression();
            expectSemicolon();
        } else if (t.kind == Kind.INT || t.kind == Kind.CHAR) {
            throw unexpected("Identifier expected at the start of an expression");
        } else {
            throw unexpected("Unexpected token '" + t.lexeme + "'");
        }
    }

    private void ifStmt() {
        next(); // if
        expectP("(", "Opening parenthesis '(' expected");
        expression();
        expectP(")", "Closing parenthesis ')' expected");
        block();
        if (isKw("else")) {
            next();
            block();
        }
    }

    private void whileStmt() {
        next(); // while
        expectP("(", "Opening parenthesis '(' expected");
        expression();
        expectP(")", "Closing parenthesis ')' expected");
        block();
    }

    private void doStmt() {
        next(); // do
        block();
        if (!isKw("while")) throw expected("Keyword 'while' expected ('do' without 'while')");
        next();
        expectP("(", "Opening parenthesis '(' expected");
        expression();
        expectP(")", "Closing parenthesis ')' expected");
        expectSemicolon();
    }

    private void forStmt() {
        next(); // for
        expectP("(", "Opening parenthesis '(' expected");
        expression();
        expectSemicolon();
        expression();
        expectSemicolon();
        expression();
        expectP(")", "Closing parenthesis ')' expected");
        if (isP("{")) block();
        else statement();
    }

    /** declaration -> type ID decl_rest ';'   (function ke andar local variables) */
    private void declaration() {
        Tok type = next();
        Tok id = expectId("Identifier expected");
        declRest(type.lexeme, id);
        expectSemicolon();
    }

    /** expression -> ID op ID | ID op constant | ID op ID op constant | ID op ID op ID */
    private void expression() {
        if (!isId()) throw expected("Identifier expected");
        next();
        if (!isOperator()) throw expected("Operator expected");
        next();
        if (isId()) {
            next();
            if (isOperator()) { // doosra operator: ID op ID op (ID | constant)
                next();
                if (isId()) next();
                else if (isConstant()) next();
                else throw expected("Identifier or integer constant expected");
            }
        } else if (isConstant()) {
            next();
        } else {
            throw expected("Identifier or integer constant expected");
        }
    }
}