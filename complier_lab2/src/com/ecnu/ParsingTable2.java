package com.ecnu;
import java.util.*;

public class ParsingTable2 {
    private static final String[] TERMNIALS = new String[]{
            "{", "}",
            "if", "(", ")", "then", "else", "while",
            "ID", "=", "NUM", ";",
            ">", "<", ">=", "<=", "==",
            "+", "-", "*", "/",
            "E",
            "$"
    };
    private static final Set<String> TERMINAL_SET = new HashSet<>(Arrays.asList(TERMNIALS));

    private static final String[] GRAMMAR_SOURCE = new String[]{
            "program -> compoundstmt",
            "stmt -> ifstmt | whilestmt | assgstmt | compoundstmt",
            "compoundstmt -> { stmts }",
            "stmts -> stmt stmts | E",
            "ifstmt -> if ( boolexpr ) then stmt else stmt",
            "whilestmt -> while ( boolexpr ) stmt",
            "assgstmt -> ID = arithexpr ;",
            "boolexpr -> arithexpr boolop arithexpr",
            "boolop -> < | > | <= | >= | ==",
            "arithexpr -> multexpr arithexprprime",
            "arithexprprime -> + multexpr arithexprprime | - multexpr arithexprprime | E",
            "multexpr -> simpleexpr multexprprime",
            "multexprprime -> * simpleexpr multexprprime | / simpleexpr multexprprime | E",
            "simpleexpr -> ID | NUM | ( arithexpr )"
    };

    private static Map<String, List<String>> grammars = new HashMap<>(); // rule
    private static Map<String, List<String>> toTerms = new HashMap<>(); // rule里的每一条，拆开

    private static Map<String, Set<String>> firstSet = new HashMap<>();
    private static Map<String, Set<String>> followSet = new HashMap<>();

    private static Map<String, Map<String, String>> table = new HashMap<>();
    private static Queue<String[]> progStream = new LinkedList<>();

    private static List<String> parseResult = new ArrayList<>();

    private static void read_prog() {
        Scanner sc = new Scanner(System.in);
        String[] prog = new String[]{"{", "ID = NUM ;", "}"};

        int counter = 1;
        int cnt = 0;
        while (cnt < 3) {
            String line = prog[cnt];
            if (line.isEmpty()) continue;
            for (String term: line.trim().split(" ")) {
                progStream.offer(new String[]{term, String.valueOf(counter)});
            }
            counter++;
            cnt++;
        }
    }

    private static void initGrammar() {
        for (String grammar: GRAMMAR_SOURCE) {
            String[] leftAndRight = grammar.split(" -> ");
            grammars.put(leftAndRight[0], new ArrayList<>());   // 创建非终止字符entry

            String[] productions = leftAndRight[1].split(" \\| "); // 将没一条产生式加入对应的entry.value中
            for (String production: productions) {
                List<String> exprs = new ArrayList<>(Arrays.asList(production.split(" ")));

                grammars.get(leftAndRight[0]).add(production);
                toTerms.put(production, exprs);
            }
        }
    }

    private static void computeFirst() {
        boolean hasChange = true;
        // 加入终结符
        for (String expr: TERMINAL_SET) {
            firstSet.put(expr, new HashSet<>(Collections.singletonList(expr)));
        }
        // 非终结符
        for (String expr: grammars.keySet()) {
            firstSet.put(expr, new HashSet<>());
        }
        // 右侧表达式
        for (String expr: toTerms.keySet()) {
            if (!firstSet.containsKey(expr)) {
                firstSet.put(expr, new HashSet<>());
            }
        }

        while (hasChange) {
            hasChange = false;
            for (String expr: grammars.keySet()) {
                List<String> productions = grammars.get(expr);
                for (String production: productions) {
                    hasChange |= firstSet.get(expr).addAll(firstSet.get(production));
                }
            }
            // 将每个expr拆分
            for (String expr: toTerms.keySet()) {
                if (TERMINAL_SET.contains(expr)) continue;
                List<String> terms = toTerms.get(expr);
                boolean hasEmpty = false;
                for (String term: terms) {
                    hasEmpty = false;
                    for (String item: firstSet.get(term)) {
                        if (!item.equals("E")) {
                            hasChange |= firstSet.get(expr).add(item);
                        } else {
                            hasEmpty = true;
                        }
                    }
                    // 如果不为空则不继续推
                    if (!hasEmpty) break;
                }
                if (hasEmpty) firstSet.get(expr).add("E");
            }
        }

        System.out.println("===========FirstSet=============");
        for (String expr: firstSet.keySet()) {
            System.out.println(expr + ":\t" + firstSet.get(expr).toString());
        }
    }

    private static void computeFollow() {
        for (String expr: grammars.keySet()) {
            followSet.put(expr, new HashSet<>());
        }
        followSet.get("program").add("$");
        boolean hasChange = true;
        while (hasChange) {
            hasChange = false;
            for (String expr: grammars.keySet()) {
                List<String> productions = grammars.get(expr);
                for (String productionString: productions) {
                    List<String> production = toTerms.get(productionString);
                    for (int i = 0; i < production.size(); i++) {
                        if (grammars.containsKey(production.get(i))) {
                            if (i + 1 < production.size()) {
                                for (String item: firstSet.get(production.get(i+1))) {
                                    if (!item.equals("E")) {
                                        hasChange |= followSet.get(production.get(i)).add(item);
                                    } else {
                                        hasChange |= followSet.get(production.get(i)).addAll(followSet.get(expr));
                                    }
                                }
                            } else {
                                hasChange |= followSet.get(production.get(i)).addAll(followSet.get(expr));
                            }
                        }
                    }
                }
            }
        }

        System.out.println("===========FollowSet=============");
        for (String expr: followSet.keySet()) {
            System.out.println(expr + ":\t" + followSet.get(expr).toString());
        }

    }

    private static void buildTable() {
        for (String expr: grammars.keySet()) {
            table.put(expr, new HashMap<>());
            for (String production: grammars.get(expr)) {
                for (String firstTerminal: firstSet.get(production)) {
                    if (firstTerminal.equals("E")) {
                        for (String followTerminal: followSet.get(expr)) {
                            table.get(expr).put(followTerminal, production);
                        }
                    } else {
                        table.get(expr).put(firstTerminal, production);
                    }
                }
            }
        }

//        System.out.println("===========Table=============");
//        for (String expr: table.keySet()) {
//            System.out.println(expr + ": " + table.get(expr).toString());
//        }

    }

    private static void analysisCore() {
        Stack<String[]> stack = new Stack<>();
        stack.push(new String[]{"$", ""});
        stack.push(new String[]{"program", ""});

        while (!progStream.isEmpty()) {
            String[] terminal = progStream.peek();
            String[] expr = stack.pop();
            parseResult.add(expr[1] + expr[0]);
//            System.out.println(expr[0] + " " + terminal[0]);

            if (expr[0].equals("E")) {

            } else if (expr[0].equals(terminal[0])) {
                progStream.poll();
            } else if (grammars.containsKey(expr[0]) && table.get(expr[0]).get(terminal[0]) != null) {
                List<String> newExprs = toTerms.get(table.get(expr[0]).get(terminal[0]));

                for (int i = newExprs.size() - 1; i >= 0; i--) {
                    stack.push(new String[]{newExprs.get(i), expr[1] + "\t"});
                }
            } else {
                if (grammars.containsKey(expr[0])) {
                    parseResult.add(expr[1] + "\t" + "E");
                } else {
                    System.out.println("语法错误,第" + (Integer.valueOf(terminal[1]) - 1) + "行,缺少\"" + expr[0] + "\"");
                }
            }
        }
    }

    private static void showResult() {
        for (String result: parseResult) {
            System.out.println(result);
        }
    }

    private static void analysis() {

        initGrammar();
        computeFirst();
        computeFollow();
        buildTable();

        read_prog();
        analysisCore();
        showResult();
    }

    public static void main(String[] args) {
        analysis();
    }
}