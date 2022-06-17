package com.ecnu;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ruleString : 符号间必须使用空格分割
 *              不能支持||符号
 */
public class ParsingTable {
    private final String[] nonTerminalString = new String[]{"program", "stmt", "compoundstmt", "stmts", "ifstmt",
            "whilestmt", "assgstmt", "boolexpr", "boolop", "arithexpr",
            "arithexprprime", "multexpr", "multexprprime", "simpleexpr"};
    private final String[] terminalString = new String[]{"{", "}", "(", ")", "if", "then", "else", "while", "ID", "=",
            "==", ">", "<", ">=", "<=", "+", "-", "*", "/", "ID", "NUM", "E", ";", "$"};
    private final String[] ruleString = new String[]{
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
    private final String begin = "program";
    private final String end = "$";

    Map<String, Map<String, String>> table;
    private Map<String, Set<String>> firstMap = new HashMap<>();

    private Map<String, Set<String>> followMap = new HashMap<>();

    private final Set<String> nonTerminal = Arrays.stream(nonTerminalString).collect(Collectors.toSet());
    private final Set<String> terminal = Arrays.stream(terminalString).collect(Collectors.toSet());

    private Map<String, Set<String>> rule;
    private List<String> res = new ArrayList<>();

    public static void main(String[] args) {
        ParsingTable parsingTable = new ParsingTable();
    }

    public ParsingTable() {
        rule = analysisRule(ruleString);

        // first
        for (String str : terminal) {
            firstMap.computeIfAbsent(str, k -> new HashSet<>()).add(str);
        }
        for (String str : nonTerminal) {
            generateFirstMap(str, rule);
        }
        for (Map.Entry<String, Set<String>> entry : rule.entrySet()) {
            Set<String> val = entry.getValue();
            for (String str : val) {
                generateFirstMap(str, rule);
            }
        }

        // 插入终结符
        followMap.computeIfAbsent(begin, k -> new HashSet<>()).add(end);
        // follow
        Set<String> visit = new HashSet<>();
        for (String str : nonTerminal) {
            generateFollowMap(str, rule, firstMap, visit);
            visit.clear();
        }

        table = generateTable(firstMap, followMap);
    }

    public void analysis(List<String> prog) {
        Deque<String[]> stack = new LinkedList<>();
        // 后面一位用来记录\t数目
        stack.push(new String[]{end, ""});
        stack.push(new String[]{begin, ""});

        for (int i = 0; i < prog.size(); i++) {
            // 得到每一行的表达式
            String[] exprs = prog.get(i).split("\\s+");
            int pointer = 0;
            while (pointer < exprs.length) {
                String expr = exprs[pointer];
                String[] top = stack.pop();

                // 保存答案
                res.add(top[1] + top[0]);

                // 栈顶为空
                if ("E".equals(top[0])) {
                    continue;
                }
                // 栈顶与当前表达式相等，则pointer移动
                if (top[0].equals(expr)) {
                    pointer++;
                } else if (nonTerminal.contains(top[0]) && table.get(top[0]).containsKey(expr)) {
                    String[] terms = generateTerm(table.get(top[0]).get(expr));

                    // 倒序插入
                    for (int j = terms.length - 1; j >= 0; j--) {
                        stack.push(new String[]{terms[j], top[1] + "\t"});
                    }
                }
                // 如果不存在对应表达式的表项
                else {
                    // 如果可以转化为空
                    if (nonTerminal.contains(top[0]) && rule.get(top[0]).contains("E")) {
                        res.add(top[1] + "\t" + "E");
                    }
                    // 忽略输入并报错
                    else {
                        System.out.println("语法错误,第" + i + "行,缺少\"" + top[0] + "\"");
                    }
                }
            }
        }
        print();
    }

    private void print() {
        for (String tmp : res) {
            System.out.println(tmp);
        }
    }

    /**
     * @param terms parsing table对应的转换表项
     * @return
     */
    private String[] generateTerm(String terms) {
        return terms.split("\\s+");
    }

    /**
     * @param firstMap
     * @param followMap
     * @return 返回parsing table
     */
    private Map<String, Map<String, String>> generateTable(Map<String, Set<String>> firstMap, Map<String, Set<String>> followMap) {
        Map<String, Map<String, String>> table = new HashMap<>();
        ;

        for (Map.Entry<String, Set<String>> entry : rule.entrySet()) {
            String left = entry.getKey();
            Set<String> val = entry.getValue();

            table.put(left, new HashMap<>());
            // 遍历表达式
            for (String right : val) {
                // 对于first(right)里的每个终结符
                for (String term1 : firstMap.get(right)) {
                    // 如果为空，则将follow(left)加入
                    if ("E".equals(term1)) {
                        for (String term2 : followMap.get(left)) {
                            table.get(left).put(term2, right);
                        }
                    } else {
                        table.get(left).put(term1, right);
                    }
                }
            }
        }

        return table;
    }

    private Map<String, Set<String>> analysisRule(String[] ruleString) {
        Map<String, Set<String>> rule = new HashMap<>();
        for (String cur : ruleString) {
            int bg = cur.indexOf("->");
            String left = cur.substring(0, bg).trim();
            String[] right = cur.substring(bg + 2).split("\\|");
            rule.put(left, new HashSet<>());

            for (String curRight : right) {
                rule.get(left).add(curRight.trim());
            }
        }
        return rule;
    }

    /**
     * 用于计算终结符和非终结符的first
     */
    private Set<String> generateFirstMap(String cur, Map<String, Set<String>> rule) {
        // 如果cur是终结符，生成set并直接返回
        if (terminal.contains(cur)) {
            firstMap.computeIfAbsent(cur, k -> new HashSet<>()).add(cur);
            return firstMap.get(cur);
        }

        Set<String> firstCur = new HashSet<>();

        // 如果是非终结符，直接计算
        if (nonTerminal.contains(cur)) {
            for (String right : rule.get(cur)) {
                String[] exprs = right.split("\\s+");

                // 是不为空的终结符，直接加入
                if (terminal.contains(exprs[0].trim()) && !"E".equals(exprs[0].trim())) {
                    firstCur.add(exprs[0].trim());
                } else {
                    int i = 0;
                    String front = exprs[i].trim();
                    firstCur.addAll(generateFirstMap(front, rule));
                    // 如果能够推出空的话，继续算下一个
                    while (("E".equals(front) || rule.get(front).contains("E")) && i < exprs.length) {
                        front = exprs[i++];
                        firstCur.addAll(generateFirstMap(front, rule));
                    }
                }
            }
        }
        // 否则拆分开
        else {
            String[] expr = cur.split("\\s+");
            firstCur = generateFirstMap(expr, rule, firstMap);
        }

        // 将结果放到firstMap里
        firstMap.computeIfAbsent(cur, k -> new HashSet<>()).addAll(firstCur);
        return firstMap.get(cur);
    }

    /**
     * 用于计算first和follow中词组的first
     */
    private Set<String> generateFirstMap(String[] cur, Map<String, Set<String>> rule, Map<String, Set<String>> firstMap) {
        Set<String> firstCur = new HashSet<>();
        for (String expr : cur) {
            // 是不为空的终结符，直接加入
            if (terminal.contains(expr.trim())) {
                firstCur.add(expr.trim());
                break;
            } else if ("E".equals(expr.trim())) {
                firstCur.add("E");
            }
            // 不能再推出空字符串
            else if (!rule.get(expr.trim()).contains("E")) {
                firstCur.addAll(firstMap.get(expr.trim()));
                break;
            } else {
                firstCur.addAll(firstMap.get(expr.trim()));
            }
        }
        return firstCur;
    }

    /**
     * 用于计算非终结符的follow
     */
    private Set<String> generateFollowMap(String cur, Map<String, Set<String>> rule, Map<String, Set<String>> firstMap, Set<String> visit) {
        // 如果cur是终结符，生成set并直接返回
        if (terminal.contains(cur)) {
            followMap.computeIfAbsent(cur, k -> new HashSet<>()).add(cur);
            return followMap.get(cur);
        }

        visit.add(cur);
        Set<String> followCur = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : rule.entrySet()) {
            String left = entry.getKey();
            Set<String> rightSet = entry.getValue();

            for (String right : rightSet) {
                // 不包含当前串
                if (!right.contains(cur)) {
                    continue;
                }
                // 以当前串结尾，注意避免自身递归
                if (right.trim().equals(cur) || (right.trim().endsWith(cur) && !Character.isAlphabetic(right.charAt(right.lastIndexOf(cur) - 1)))) {
                    if (!visit.contains(left)) {
                        followCur.addAll(generateFollowMap(left, rule, firstMap, visit));
                    } else if (followMap.get(left) != null) {
                        followCur.addAll(followMap.get(left));
                    }
                }
                // 遍历找当前串后面的串
                String[] expr = right.split("\\s+");
                for (int i = 0; i < expr.length - 1; i++) {
                    String tmp = expr[i].trim();

                    if (tmp.equals(cur)) {
                        // 后面不为空，加上后面的firstMap
                        if (firstMap.get(expr[i + 1].trim()) != null && !firstMap.get(expr[i + 1].trim()).contains("E")) {
                            followCur.addAll(firstMap.get(expr[i + 1].trim()));
                        }
                        // 后面为空
                        else {
                            String[] behind = Arrays.copyOfRange(expr, i + 1, expr.length);
                            Set<String> behindFirst = generateFirstMap(behind, rule, firstMap);

                            // 如果为空，加上后面的firstMap和左边的followMap
                            if (behindFirst.contains("E")) {
                                if (!visit.contains(left)) {
                                    followCur.addAll(generateFollowMap(left, rule, firstMap, visit));
                                } else if (followMap.get(left) != null) {
                                    followCur.addAll(followMap.get(left));
                                }
                            }

                            followCur.addAll(behindFirst);
                            followCur.remove("E");
                        }
                    }
                }
            }
        }
        visit.remove(cur);
        followMap.computeIfAbsent(cur, k -> new HashSet<>()).addAll(followCur);
        return followMap.get(cur);
    }
}