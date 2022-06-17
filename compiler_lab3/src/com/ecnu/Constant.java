package com.ecnu;

class Constant {
    static final String[] nonTerminalString = new String[]{"program'", "program", "stmt", "compoundstmt", "stmts", "ifstmt",
            "whilestmt", "assgstmt", "boolexpr", "boolop", "arithexpr",
            "arithexprprime", "multexpr", "multexprprime", "simpleexpr"};

    static final String[] terminalString = new String[]{"{", "}", "(", ")", "if", "then", "else", "while", "ID", "=",
            "==", ">", "<", ">=", "<=", "+", "-", "*", "/", "ID", "NUM", "E", ";", "$"};

    static final String[] ruleString = new String[]{
            "program' -> program",
            "program -> compoundstmt",
            "stmt ->  ifstmt  |  whilestmt  |  assgstmt  |  compoundstmt",
            "compoundstmt ->  { stmts }",
            "stmts ->  stmt stmts   |   E",
            "ifstmt ->  if ( boolexpr ) then stmt else stmt",
            "whilestmt ->  while ( boolexpr ) stmt",
            "assgstmt ->  ID = arithexpr ;",
            "boolexpr  ->  arithexpr boolop arithexpr",
            "boolop ->   <  |  >  |  <=  |  >=  | ==",
            "arithexpr  ->  multexpr arithexprprime",
            "arithexprprime ->  + multexpr arithexprprime  |  - multexpr arithexprprime  |   E",
            "multexpr ->  simpleexpr  multexprprime",
            "multexprprime ->  * simpleexpr multexprprime  |  / simpleexpr multexprprime  |   E",
            "simpleexpr ->  ID  |  NUM  |  ( arithexpr )"
    };

    static final String begin = "program";
    static final String end = "$";
    static final String empty = "E";

    /**
     * 语法分析的三个状态，S、R、Acc和GOTO
     */
    enum ActionState {
        SHIFT,
        REDUCE,
        ACCEPT
    }
}
