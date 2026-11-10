package com.project.lexerjava;

public class ParserDemo {
    public static void main(String[] args) {
        String src = """
                void main(void)
                {
                 int a,b;
                 char c,d;
                 a+b; c= b+a;
                 if (c>10) {a=b}
                 else {a=5}
                }
                """;

        var lexResult = new LexerEngine().tokenize(src);
        var result = new SyntaxParser().parse(lexResult.getTokens());

        System.out.println("Lexeme\tType\tScope\tArray");
        for (SymbolEntry s : result.getSymbols()) System.out.println(s);
        System.out.println("Errors: " + result.getErrors().size());
        for (SyntaxError e : result.getErrors()) System.out.println(e);
    }
}