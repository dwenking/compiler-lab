package com.ecnu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyScanner {
    private StringBuffer prog;
    private int begin;
    // 标记字符串状态
    private int state;
    private int currentId;
    private List<Token> tokenList;

    public MyScanner(StringBuffer prog) {
        this.prog = prog;
        this.begin = 0;
        this.state = 0;
        this.currentId = 1;
        tokenList = new ArrayList<>();
    }

    public void analysis() {
        while (begin < prog.length()) {
            Token token = nextToken();
            if (token != null) {
                tokenList.add(token);
                token.print();
            }
        }
    }

    private Token nextToken() {
        while (checkNext() == CharType.BLANK || checkNext() == CharType.ENDL) {
            get();
        }
        if (state != 0) {
            return generateString();
        }
        switch (checkNext()) {
            case ALPHABET:
                return generateKeyOrIdentifier();
            case NUMBER:
                return generateNumber();
            case SLASH:
                return generateOperatorOrComment();
            case DOT:
            case SYMBOL:
                return generateOperator();
            case QUOTE:
                return generateString();
            default:
        }
        return null;
    }

    private Token generateString() {
        StringBuffer s = new StringBuffer();
        switch (this.state) {
            case (0) :
                // 需要单独拆分”号
                s.append(get());
                this.state = 1;
                return new Token(Token.TokenType.OPERATER, s.toString(), currentId++);
            case (1) :
                while (checkNext() != CharType.QUOTE) {
                    s.append(get());
                }
                this.state = 2;
                return new Token(Token.TokenType.IDENTIFIER, s.toString(), currentId++);
            case (2) :
                // 需要单独拆分”号
                s.append(get());
                this.state = 0;
                return new Token(Token.TokenType.OPERATER, s.toString(), currentId++);
            default:
        }
        return null;
    }

    private Token generateOperator() {
        StringBuffer s = new StringBuffer();
        short state = 0;
        while (true) {
            switch (state) {
                case 0:
                    switch (prog.charAt(begin)) {
                        case '(':
                        case ')':
                        case ',':
                        case '.':
                        case ':':
                        case ';':
                        case '?':
                        case '[':
                        case ']':
                        case '{':
                        case '}':
                        case '~':
                            s.append(get());
                            return new Token(Token.TokenType.OPERATER, s.toString(), currentId++);
                        default: // + - = | & ^ < >
                            s.append(get());
                            if (checkNext() == CharType.SYMBOL) {
                                state = 1;
                            } else {
                                return new Token(Token.TokenType.OPERATER, s.toString(), currentId++);
                            }
                    }
                    break;
                case 1:
                    switch (prog.charAt(begin)) {
                        case '+':
                        case '-':
                        case '=':
                        case '&':
                        case '|':
                            s.append(get());
                            return new Token(Token.TokenType.OPERATER, s.toString(), currentId++);
                        default:
                            s.append(get());
                            if (checkNext() == CharType.SYMBOL) {
                                state = 2;
                            }
                            else {
                                return new Token(Token.TokenType.OPERATER, s.toString(), currentId++);
                            }
                    }
                    break;
                case 2:
                    s.append(get());
                    return new Token(Token.TokenType.OPERATER, s.toString(), currentId++);
                default:
            }
        }
    }

    private Token generateOperatorOrComment() {
        StringBuffer s = new StringBuffer();
        char cur = get();
        s.append(cur);
        short state = 0;

        while (true) {
            switch (state) {
                case 0:
                    switch (prog.charAt(begin)) {
                        case '=':
                            s.append(get());
                            return new Token(Token.TokenType.OPERATER, s.toString(), currentId++);
                        case '/':
                            s.append(get());
                            state = 1;
                            break;
                        case '*':
                            s.append(get());
                            state = 2;
                            break;
                        default:
                            return new Token(Token.TokenType.OPERATER, s.toString(), currentId++);
                    }
                    break;
                case 1:
                    switch (checkNext()) {
                        case ENDL:
                            get();
                            return new Token(Token.TokenType.COMMENT, s.toString(), currentId++);
                        default:
                            s.append(get());
                    }
                    break;
                case 2:
                    switch (checkNext()) {
                        case STAR:
                            s.append(get());
                            state = 3;
                            break;
                        default:
                            s.append(get());
                    }
                    break;
                case 3:
                    switch (checkNext()) {
                        case SLASH:
                            s.append(get());
                            return new Token(Token.TokenType.COMMENT, s.toString(), currentId++);
                        default:
                            s.append(get());
                            state = 2;
                    }
                    break;
                default:
                    System.out.println("状态出现异常！");
            }
        }
    }

    private Token generateNumber() {
        StringBuffer s = new StringBuffer();
        short state = 0;

        while (true) {
            switch (state) {
                case 0:
                    switch (checkNext()) {
                        case NUMBER:
                            s.append(get());
                            state = 0;
                            break;
                        case DOT:
                            s.append(get());
                            state = 1;
                            break;
                        default:
                            return new Token(Token.TokenType.CONSTANT, s.toString(), currentId++);
                    }
                    break;
                case 1:
                    switch(checkNext()) {
                        case NUMBER:
                            s.append(get());
                            state = 0;
                            break;
                        default:
                            return new Token(Token.TokenType.CONSTANT, s.toString(), currentId++);
                    }
                    break;
                default:
                    System.out.println("状态出现异常！");
            }
        }
    }

    private Token generateKeyOrIdentifier() {
        StringBuffer s = new StringBuffer();

        while (checkNext() == CharType.ALPHABET || checkNext() == CharType.NUMBER) {
            s.append(get());
        }

        if (keySet.contains(s.toString())) {
            return new Token(Token.TokenType.KEYWORD, s.toString(), currentId++);
        }
        else {
            return new Token(Token.TokenType.IDENTIFIER, s.toString(), currentId++);
        }
    }

    private CharType checkNext() {
        if (begin >= prog.length()) {
            return CharType.EOF;
        }
        char next = prog.charAt(begin);
        if (Character.isAlphabetic(next)) {
            return CharType.ALPHABET;
        }
        if (Character.isDigit(next)) {
            return CharType.NUMBER;
        }

        switch (next) {
            case ' ':
            case '\t':
                return CharType.BLANK;
            case '\n':
            case '\r':
                return CharType.ENDL;
            case '.':
                return CharType.DOT;
            case '*':
                return CharType.STAR;
            case '/':
                return CharType.SLASH;
            case '"':
                return CharType.QUOTE;
            case '\0':
                return CharType.EOF;
            default:
                return CharType.SYMBOL;
        }
    }

    private Character get() {
        return prog.charAt(begin++);
    }

    private static final Set<String> keySet = new HashSet<String>(){{
        add("auto");
        add("break");
        add("case");
        add("char");
        add("const");
        add("continue");
        add("default");
        add("do");
        add("double");
        add("else");
        add("enum");
        add("extern");
        add("float");
        add("for");
        add("goto");
        add("if");
        add("int");
        add("long");
        add("register");
        add("return");
        add("short");
        add("signed");
        add("sizeof");
        add("static");
        add("struct");
        add("switch");
        add("typedef");
        add("union");
        add("unsigned");
        add("void");
        add("volatile");
        add("while");
    }};

    enum CharType{
        ALPHABET,
        NUMBER,
        SYMBOL,
        SLASH,
        STAR,
        DOT,
        QUOTE,
        BLANK,
        ENDL,
        EOF;
    }
}
