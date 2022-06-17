package com.ecnu;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Java_TranslationSchemaAnalysis
{
    static int num = 1;
    static Variable root;
    private static List<String[]> exprs = new ArrayList<>();
    private static List<LineToken> tokens = new ArrayList<>();

    static Map<String, List<String>> expMap = new HashMap<>();
    static Map<String, Set<String>> firstMap = new HashMap<>();
    static Map<String, Set<String>> firstMap2 = new HashMap<>();
    static Map<String, Set<String>> followMap = new HashMap<>();

    static Map<String,Integer> expNumberMap = new HashMap<>();
    static Map<Integer,String> NumberExp = new HashMap<>();
    static Map<String,Integer> tableMap = new HashMap<>();

    static Deque<Variable> stack = new LinkedList<>();

    static Variable wrong;

    static Deque<String> valueQueue = new LinkedList<>();
    static Deque<String> idQueue = new LinkedList<>();
    static Deque<Variable> output = new LinkedList<>();

    private static final Set<String> nonTerminalSet = new HashSet<>();
    private static final Set<String> terminalSet = new HashSet<>();
    /**
     *  this method is to read the standard input
     */
    private static void read_prog() {
        int cnt = 1;
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.length() == 0 || "".equals(line)) {
                continue;
            }
            for (String str : line.split("\\s+")) {
                exprs.add(new String[]{str, String.valueOf(cnt)});
            }
            cnt++;
        }
    }

    /**
     *  you should add some code in this method to achieve this lab
     */
    private static void analysis() throws Exception {
        read_prog();
        init();

        // 处理输入数据，变成带行号的token
        for (String[] expr : exprs) {
            String[] tmp = expr[0].trim().split("\\s+");
            for (String token : tmp) {
                tokens.add(new LineToken(Integer.parseInt(expr[1]), token));
            }
        }

        for (String nonTerm : nonTerminalSet) {
            generateFirstOnce(nonTerm);
        }
        followMap = generateFollow(nonTerminalSet);

        generateParsingTable();
        analysisCore();

        root = generateTree();

        ValTranslator valTranslator = new ValTranslator(root, idQueue, valueQueue);
        valTranslator.program();

        num = 1;
    }
    /**
     * this is the main method
     * @param args
     */
    public static void main(String[] args) throws Exception {
        analysis();
    }

    /**
     * 分析主函数
     */
    private static void analysisCore() {
        stack.push(new Variable(0,Constant.end));
        stack.push(new Variable(0,"program"));
        Variable top = stack.peek();

        int pos = 0;
        String type = "";

        while(!top.token.equals(Constant.end)){
            LineToken cur = tokens.get(pos);
            if(cur.token.length() == 1 && Character.isLowerCase(cur.token.charAt(0)))
            {
                idQueue.add(cur.token);
                cur.token = "ID";
            }
            else if(cur.token.equals("int")){
                type = "INTNUM";
            }
            else if(cur.token.equals("real")){
                type="REALNUM";
            }
            else if(Constant.isInt(cur.token)|| Constant.isDouble(cur.token)){
                valueQueue.add(cur.token);
                cur.token = type;
            }
            if (top.token.equals(cur.token)) {
                Variable out = stack.pop();
                output.add(out);
                pos++;
            }
            else if(tableMap.containsKey(top.token+"->"+cur.token)){
                String replace = NumberExp.get(tableMap.get(top.token+"->"+cur.token));
                String[] r = replace.split("->");
                String[] re = r[1].trim().split("\\s+");
                Variable out = stack.pop();
                output.add(out);

                for(int i=re.length-1;i>=0;i--){
                    if(!re[i].equals("")) {
                        stack.push(new Variable(top.num + 1, re[i]));
                    }
                }
            }
            else if(terminalSet.contains(top.token)) {
                Variable out = stack.pop();
                output.add(new Variable(out.num,out.token));
            }
            else {
                String replace = NumberExp.get(tableMap.get(top.token + "->" + ";"));
                String[] r = replace.split("->");
                String[] re = r[1].trim().split("\\s+");

                Variable out = stack.pop();
                wrong = new Variable(cur.lineNumber - 1, stack.peek().token);
                output.add(out);

                for(int i = re.length - 1;i >= 0; i--){
                    if(!re[i].equals("")) {
                        stack.push(new Variable(top.num + 1, re[i]));
                    }
                }
            }
            top = stack.peek();
        }
    }

    /**
     * 根据First和Follow函数结果生成表
     */
    private static void generateParsingTable() {
        for(String nonTerm : nonTerminalSet){
            for(String ss : expMap.get(nonTerm)){
                Set<String> first = getFirst(ss);
                for(String s : first){
                    tableMap.put(nonTerm + "->" + s,expNumberMap.get(nonTerm + "->" + ss));
                }
                if(first.contains(Constant.empty)){
                    for(String s : followMap.get(nonTerm)){
                        tableMap.put(nonTerm + "->" + s,expNumberMap.get(nonTerm + "->E"));
                    }
                }
            }
        }
    }

    /**
     * 根据文法进行初始化
     */
    private static void init() {
        for (String rule : Constant.ruleString) {
            // 提取所有token
            String[] token = rule.split("-> | [|]");
            List<String> tmp = new ArrayList<>();
            String right = token[0].trim();

            for (int i = 1; i < token.length; i++) {
                tmp.add(token[i].trim());
                for(String aa : token[i].trim().split("\\s+")) {
                    if(!aa.equals("")) {
                        terminalSet.add(aa.trim());
                    }
                }

                expNumberMap.put(right + "->" + token[i].trim(), num);
                NumberExp.put(num++, right +"->" + token[i].trim());
            }

            expMap.put(right, tmp);
            nonTerminalSet.add(right);
            terminalSet.add(right);
        }

        for(String nonT : nonTerminalSet){
            terminalSet.remove(nonT);
        }
        terminalSet.add("E");
    }

/*    *//**
     * 计算Fisrt函数
     * @param nonTerminalSet
     * @return
     *//*
    private static void generateFirst(Set<String> nonTerminalSet) {
        for (String nonTerm : nonTerminalSet) {
            if (firstMap.containsKey(nonTerm)) {
                continue;
            }

            HashSet<String> tmp = new HashSet<>();

            if (terminalSet.contains(nonTerm)) {
                tmp.add(nonTerm);
                firstMap.put(nonTerm, tmp);
                continue;
            }

            for (String s : expMap.get(nonTerm)) {
                if (Constant.empty.equals(nonTerm)) {
                    tmp.add(Constant.empty);
                } else {
                    for (String temp : s.split("\\s+")) {
                        if (!firstMap.containsKey(temp)) {
                            generateFirstOnce(temp);
                        }
                        tmp.addAll(firstMap.get(temp));
                        if (!firstMap.get(temp).contains(Constant.empty)) {
                            tmp.remove(Constant.empty);
                            break;
                        }
                    }
                }
            }

            firstMap.put(nonTerm, tmp);
        }
    }*/

    /**
     * 计算某个字串的First
     * @param nonTerm
     */
    private static void generateFirstOnce(String nonTerm) {
        if (firstMap.containsKey(nonTerm)) {
            return;
        }

        HashSet<String> tmp = new HashSet<>();

        if (terminalSet.contains(nonTerm)) {
            tmp.add(nonTerm);
            firstMap.put(nonTerm, tmp);
            return;
        }

        for (String s : expMap.get(nonTerm)) {
            if (Constant.empty.equals(nonTerm)) {
                tmp.add(Constant.empty);
            }
            else {
                for (String temp : s.split("\\s+")) {
                    if (!firstMap.containsKey(temp)) {
                        generateFirstOnce(temp);
                    }
                    tmp.addAll(firstMap.get(temp));
                    if (!firstMap.get(temp).contains(Constant.empty)) {
                        tmp.remove(Constant.empty);
                        break;
                    }
                }
            }
        }

        firstMap.put(nonTerm, tmp);
    }

    /**
     * 计算Follow函数
     */
    private static Map<String, Set<String>> generateFollow(Set<String> nonTerminalSet) {
        Map<String, Set<String>> followMap = new HashMap<>();

        for (String nonTerm1 : nonTerminalSet) {
            Set<String> followBegin = followMap.computeIfAbsent(nonTerm1, key -> new HashSet<>());;
            for (String nonTerm2 : nonTerminalSet) {
                for (String s : expMap.get(nonTerm2)) {
                    String[] e = s.split("\\s+");

                    for (int i = 0; i < e.length - 1; i++) {
                        if(nonTerm1.equals(e[i])){
                            StringBuilder tmp = new StringBuilder();
                            for(int j = i + 1; j < e.length; j++) {
                                tmp.append(e[j]+" ");
                            }

                            Set<String> tt = getFirst(tmp.toString().trim());
                            if(tt != null && !tt.contains(Constant.empty)) {
                                followBegin.addAll(tt);
                            }
                        }
                    }
                }
            }
            followMap.put(nonTerm1, followBegin);
        }

        followMap.get(Constant.begin).add(Constant.end);

        int cnt = 2;
        while(cnt != 0) {
            cnt--;
            for (String exp : nonTerminalSet) {
                List<String> exps = expMap.get(exp);

                for(String s : exps){
                    String[] ss = s.split("\\s+");

                    for(int i = ss.length - 1;i >= 0;i--){
                        if(nonTerminalSet.contains(ss[i]) && i < ss.length - 1){
                            HashSet<String> follow = new HashSet<>();
                            StringBuilder temp = new StringBuilder();
                            for(int j = i+1;j < ss.length; j++) {
                                temp.append(ss[j]+" ");
                            }
                            Set<String> first = getFirst(temp.toString().trim());
                            if(first.contains(Constant.empty)){
                                first.remove(Constant.empty);
                                follow.addAll(first);
                                follow.addAll(followMap.get(exp));
                                follow.addAll(followMap.get(ss[i]));
                                followMap.put(ss[i],follow);
                            }
                        }
                        else if(nonTerminalSet.contains(ss[i]) && i == ss.length - 1){
                            HashSet<String> follow = new HashSet<>();
                            follow.addAll(followMap.get(exp));
                            follow.addAll(followMap.get(ss[i]));
                            followMap.put(ss[i],follow);
                        }
                    }
                }
            }
        }

        return followMap;
    }

    /**
     * 返回First结果
     * @param tokens
     * @return
     */
    private static Set<String> getFirst(String tokens){
        if("".equals(tokens) || " ".equals(tokens)) {
            return null;
        }
        Set<String> set = new HashSet<>();
        if (firstMap2.containsKey(tokens)) {
            return new HashSet<>(firstMap2.get(tokens));
        }

        String[] aa = tokens.split("\\s+");
        for(int i = 0; i < aa.length; i++){
            if(!"".equals(aa[i])){
                if (!firstMap.containsKey(aa[i])) {
                    generateFirstOnce(aa[i]);
                }

                Set<String> tmp = firstMap.get(aa[i]);
                set.addAll(tmp);

                if (tmp.contains(Constant.empty)) {
                    i++;
                }
                else {
                    break;
                }

                if (i == aa.length) {
                    set.add(Constant.empty);
                }
            }
        }
        firstMap2.put(tokens,set);
        return set;
    }

    private static Variable generateTree(){
        Variable root = output.poll();
        Variable tmp = root;
        while(!output.isEmpty()){
            for(Variable p : output){
                assert tmp != null;
                if(p.num == tmp.num + 1) {
                    tmp.child.add(p);
                }
                else if (p.num < tmp.num + 1) {
                    break;
                }
            }
            tmp = output.poll();
        }
        return root;
    }
}

class ValTranslator {
    Variable root;
    ArrayList<String> wr = new ArrayList<>();
    Map<String,String> values = new HashMap<>();
    Deque<String> valueQueue;
    Deque<String> idQueue;

    public ValTranslator(Variable root, Deque<String> idQueue, Deque<String> valueQueue) {
        this.root = root;
        this.idQueue = idQueue;
        this.valueQueue = valueQueue;
    }

    void program(){
        decls(root.child.get(0));
        compoundstmt(root.child.get(1));
        myprint();
    }

    private void myprint() {
        int i = 0;
        if(wr.isEmpty()) {
            for (String key : values.keySet()) {
                System.out.print(key + ": " + values.get(key));
                i++;
                if (i != values.keySet().size()) {
                    System.out.print("\n");
                }
            }
        }
        else{
            for (String key : wr) {
                System.out.print(key);
                i++;
                if (i != wr.size()) {
                    System.out.print("\n");
                }
            }
        }
    }

    private void compoundstmt(Variable root) {
        stmts(root.child.get(1));
    }

    private void stmts(Variable root) {
        if(root.child.size()!=1){
            stmt(root.child.get(0));
            stmts(root.child.get(1));
        }
    }

    private void stmt(Variable root) {
        switch (root.child.get(0).token) {
            case "ifstmt":
                ifstmt(root.child.get(0));
                break;
            case "assgstmt":
                assgstmt(root.child.get(0));
                break;
            case "compoundstmt":
                compoundstmt(root.child.get(0));
                break;
        }
    }

    private void assgstmt(Variable root) {
        String ID = idQueue.poll();
        Variable ret = arithexpr(root.child.get(2));
        assert ret != null;
        values.put(ID,ret.value);
    }

    private Variable arithexpr(Variable root) {
        return arithexprprime(multexpr(root.child.get(0)),root.child.get(1));
    }

    private Variable arithexprprime(Variable before, Variable root) {
        if(root.child.size()==1){
            return before;
        } else if(root.child.get(0).token.equals("+")){
            root.child.get(1).value = multexpr(root.child.get(1)).value;
            if(Constant.isDouble(before.value)|| Constant.isDouble(root.child.get(1).value)){
                double ss = Double.parseDouble(before.value)+Double.parseDouble(root.child.get(1).value);
                Variable sum = new Variable(root.num,"sum");
                sum.value = String.valueOf(ss);
                return arithexprprime(sum, root.child.get(2));
            }
            else{
                int ss = Integer.parseInt(before.value)+Integer.parseInt(root.child.get(1).value);
                Variable sum = new Variable(root.num,"sum");
                sum.value = String.valueOf(ss);
                return arithexprprime(sum,root.child.get(2));
            }

        } else if(root.child.get(0).token.equals("-")){
            root.child.get(1).value = multexpr(root.child.get(1)).value;
            if(Constant.isDouble(before.value)||Constant.isDouble(root.child.get(1).value)){
                double ss = Double.parseDouble(before.value)-Double.parseDouble(root.child.get(1).value);
                Variable sub = new Variable(root.num,"sub");
                sub.value = String.valueOf(ss);
                return arithexprprime(sub,root.child.get(2));
            }
            else {
                int ss = Integer.parseInt(before.value)-Integer.parseInt(root.child.get(1).value);
                Variable sub = new Variable(root.num,"sub");
                sub.value = String.valueOf(ss);
                return arithexprprime(sub,root.child.get(2));
            }
        }
        return null;
    }

    private Variable multexpr(Variable root) {
        return multexprprime(simpleexpr(root.child.get(0)),root.child.get(1));
    }

    private Variable multexprprime(Variable before, Variable root) {
        if(root.child.size()==1){
            return before;
        } else if(root.child.get(0).token.equals("*")){
            root.child.get(1).value = simpleexpr(root.child.get(1)).value;
            if(Constant.isDouble(before.value)||Constant.isDouble(root.child.get(1).value)){
                double ss = Double.parseDouble(before.value)*Double.parseDouble(root.child.get(1).value);
                Variable sum = new Variable(root.num,"mul");
                sum.value = String.valueOf(ss);
                return multexprprime(sum,root.child.get(2));
            } else{
                int ss = Integer.parseInt(before.value)*Integer.parseInt(root.child.get(1).value);
                Variable sum = new Variable(root.num,"mul");
                sum.value = String.valueOf(ss);
                return multexprprime(sum ,root.child.get(2));
            }
        } else if(root.child.get(0).token.equals("/")){
            root.child.get(1).value = simpleexpr(root.child.get(1)).value;
            if(Constant.isDouble(before.value)||Constant.isDouble(root.child.get(1).value)){
                if(Double.parseDouble(root.child.get(1).value)==0){
                    wr.add("error message:line 5,division by zero");
                    root.child.get(1).value = "1";
                }
                double ss = Double.parseDouble(before.value)/Double.parseDouble(root.child.get(1).value);
                Variable sub = new Variable(root.num,"div");
                sub.value = String.valueOf(ss);
                return multexprprime(sub,root.child.get(2));
            } else{
                int ss = Integer.parseInt(before.value)/Integer.parseInt(root.child.get(1).value);
                Variable sub = new Variable(root.num,"div");
                sub.value = String.valueOf(ss);
                return multexprprime(sub, root.child.get(2));
            }
        }
        return null;
    }

    private Variable simpleexpr(Variable root) {
        Variable ret = new Variable(root.num,"simp");
        switch (root.child.get(0).token) {
            case "ID":
                ret.value = values.get(idQueue.poll());
                break;
            case "INTNUM":
                ret.value = valueQueue.poll();
                break;
            case "REALNUM":
                ret.value = valueQueue.poll();
                break;
            case "(":
                ret = arithexpr(root.child.get(1));
                break;
        }
        return ret;
    }

    private void ifstmt(Variable root) {
        if(boolexpr(root.child.get(2))) {
            stmt(root.child.get(5));
            idQueue.poll();
            idQueue.poll();
            valueQueue.poll();
        }
        else {
            idQueue.poll();
            idQueue.poll();
            valueQueue.poll();
            stmt(root.child.get(7));
        }
    }

    private Boolean boolexpr(Variable root) {
        return boolop(root.child.get(1),arithexpr(root.child.get(0)),arithexpr(root.child.get(2)));
    }

    private Boolean boolop(Variable pair, Variable arithexpr, Variable arithexpr1) {
        String bb  = pair.child.get(0).token;
        int v1=1;
        int v2=2;
        int val1=1;
        int val2=1;
        double val3=1.0;
        double val4=1.0;
        if(Constant.isInt(arithexpr.value)) {
            val1 = Integer.parseInt(arithexpr.value);
        } else {
            val3 = Double.parseDouble(arithexpr.value);
            v1=3;
        }
        if(Constant.isInt(arithexpr1.value)) {
            val2 = Integer.parseInt(arithexpr1.value);
        } else {
            val4 = Double.parseDouble(arithexpr1.value);
            v2=4;
        }
        switch (bb){
            case "<":{
                return (v1==1?val1:val3)<(v2==2?val2:val4);
            }
            case ">":{
                return (v1==1?val1:val3)>(v2==2?val2:val4);
            }
            case "<=":{
                return (v1==1?val1:val3)<=(v2==2?val2:val4);
            }
            case ">=":{
                return (v1==1?val1:val3)>=(v2==2?val2:val4);
            }
            case "==":{
                return (v1==1?val1:val3)==(v2==2?val2:val4);
            }
            default:{
                return true;
            }
        }
    }

    private void decls(Variable root) {
        if(root.child.size()!=1){
            decl(root.child.get(0));
            decls(root.child.get(2));
        }
    }

    private void decl(Variable root) {
        if(root.child.get(0).token.equals("int")||root.child.get(0).token.equals("real")){
            typecheck(root);
            values.put(idQueue.poll(),valueQueue.poll());
        }
    }

    private void typecheck(Variable root) {
        String ID = idQueue.peek();
        String value = valueQueue.peek();
        if(Constant.isDouble(value)){
            if(root.child.get(0).token.equals("int")){
                error(1);
            }
        } else if(Constant.isInt(value)){
            if(root.child.get(0).token.equals("real")){
                error(2);
            }
        }
    }

    private void error(int errorCode) {
        wr.add("error message:line 1,realnum can not be translated into int type");
    }
}

class LineToken{
    int lineNumber;
    String token;

    public LineToken(int lineNumber, String token) {
        this.lineNumber = lineNumber;
        this.token = token;
    }
}

class Variable {
    String token;
    String value;
    int num;
    List<Variable> child;

    public Variable(int num, String token) {
        this.num = num;
        this.token = token;
        this.child = new ArrayList<>();
    }
}

class Constant {
    static final String[] nonTerminalString = new String[]{"program'", "program", "stmt", "compoundstmt", "stmts", "ifstmt",
            "assgstmt", "boolexpr", "boolop", "arithexpr",
            "arithexprprime", "multexpr", "multexprprime", "simpleexpr", "decls", "decl"};

    static final String[] terminalString = new String[]{"{", "}", "(", ")", "if", "then", "else", "=",
            "==", ">", "<", ">=", "<=", "+", "-", "*", "/", "ID", "INTNUM", "REALNUM", "E", ";", "$", "int", "real", "a", "b", "c",
            "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    static final String[] ruleString = new String[]{
            "program' -> program",
            "program -> decls compoundstmt",
            "decls -> decl ; decls | E",
            "decl -> int ID = INTNUM | real ID = REALNUM",
            "stmt -> ifstmt | assgstmt | compoundstmt",
            "compoundstmt -> { stmts }",
            "stmts -> stmt stmts | E",
            "ifstmt -> if ( boolexpr ) then stmt else stmt",
            "assgstmt ->  ID = arithexpr ;",
            "boolexpr  ->  arithexpr boolop arithexpr",
            "boolop ->   <  |  >  |  <=  |  >=  | ==",
            "arithexpr  ->  multexpr arithexprprime",
            "arithexprprime ->  + multexpr arithexprprime  |  - multexpr arithexprprime  |   E",
            "multexpr ->  simpleexpr  multexprprime",
            "multexprprime ->  * simpleexpr multexprprime  |  / simpleexpr multexprprime  |   E",
            "simpleexpr -> ID | INTNUM | REALNUM | ( arithexpr )"
    };

    static final String begin = "program'";
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

    /**
     * 判断输入是否为整数
     * @param str
     * @return
     */
    public static boolean isInt(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

    public static boolean isDouble(String str) {
        for(char i : str.toCharArray()){
            if(i=='.'){
                return true;
            }
        }
        return false;
    }
}