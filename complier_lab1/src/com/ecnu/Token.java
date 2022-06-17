package com.ecnu;

import java.util.HashMap;
import java.util.Map;

public class Token {
    private TokenType type;
    private String content;
    private int id;

    public Token(TokenType type, String content, int id) {
        this.type = type;
        this.content = content;
        this.id = id;
    }

    public void print() {
        if (id != 1) {
            System.out.println();
        }
        System.out.printf("%d: <%s,%d>", this.id, this.content, getMapId());
    }

    private int getMapId() {
        switch (this.type) {
            case COMMENT:
                return tokenMap.get("Comment");
            case CONSTANT:
                return tokenMap.get("Constant");
            case IDENTIFIER:
                return tokenMap.get("Identifier");
            default:
                return tokenMap.get(content) == null ? -1 : tokenMap.get(content);
        }
    }

    private static final Map<String, Integer> tokenMap = new HashMap<String, Integer>(){{
        put("auto",       1);
        put("break",      2);
        put("case",       3);
        put("char",       4);
        put("const",      5);
        put("continue",   6);
        put("default",    7);
        put("do",         8);
        put("double",     9);
        put("else",       10);
        put("enum",       11);
        put("extern",     12);
        put("float",      13);
        put("for",        14);
        put("goto",       15);
        put("if",         16);
        put("int",        17);
        put("long",       18);
        put("register",   19);
        put("return",     20);
        put("short",      21);
        put("signed",     22);
        put("sizeof",     23);
        put("static",     24);
        put("struct",     25);
        put("switch",     26);
        put("typedef",    27);
        put("union",      28);
        put("unsigned",   29);
        put("void",       30);
        put("volatile",   31);
        put("while",      32);
        put("-",          33);
        put("--",         34);
        put("-=",         35);
        put("->",         36);
        put("!",          37);
        put("!=",         38);
        put("%",          39);
        put("%=",         40);
        put("&",          41);
        put("&&",         42);
        put("&=",         43);
        put("(",          44);
        put(")",          45);
        put("*",          46);
        put("*=",         47);
        put(",",          48);
        put(".",          49);
        put("/",          50);
        put("/=",         51);
        put(":",          52);
        put(";",          53);
        put("?",          54);
        put("[",          55);
        put("]",          56);
        put("^",          57);
        put("^=",         58);
        put("{",          59);
        put("|",          60);
        put("||",         61);
        put("|=",         62);
        put("}",          63);
        put("~",          64);
        put("+",          65);
        put("++",         66);
        put("+=",         67);
        put("<",          68);
        put("<<",         69);
        put("<<=",        70);
        put("<=",         71);
        put("=",          72);
        put("==",         73);
        put(">",          74);
        put(">=",         75);
        put(">>",         76);
        put(">>=",        77);
        put("\"",         78);
        put("Comment",    79);
        put("Constant",   80);
        put("Identifier", 81);
    }};

    enum TokenType {
        KEYWORD,
        OPERATER,
        COMMENT,
        CONSTANT,
        IDENTIFIER;
    }
}