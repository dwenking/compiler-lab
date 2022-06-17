package com.ecnu;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 构造LR文法中的Parsing Table并分析
 */
public class ParsingTable {
    Map<Integer, Map<String, Action>> actionTable;
    Map<Integer, Map<String, Integer>> gotoTable;

    private final List<Statement> statements;
    private final Map<String, Set<String>> first;
    private final Map<Set<Item>, Integer> carnonical;

    public ParsingTable() throws Exception {
        // 执行初始化步骤
        this.gotoTable = new HashMap<>();
        this.statements = generateStatement();
        this.first = computeFirst(this.statements);
        this.carnonical = generateItems(this.statements, this.gotoTable);
        this.actionTable = generateTable(this.carnonical, this.gotoTable);
    }

//    public static void main(String[] args) throws Exception {
//        ParsingTable table = new ParsingTable();
//        List<String[]> exprs = new ArrayList<>();
//
//        exprs.add(new String[]{"{", "0"});
//        exprs.add(new String[]{"ID", "1"});
//        exprs.add(new String[]{"=", "1"});
//        exprs.add(new String[]{"NUM", "1"});
//        exprs.add(new String[]{"}", "2"});
//
//        table.analysis(exprs);
//    }

    /**
     * 主程序，根据输入的prog输出分析
     * @param exprs 输入的程序
     */
    public void analysis(List<String[]> exprs) {
        Deque<Integer> state = new LinkedList<>();
        Deque<String> symbol = new LinkedList<>();

        List<String> res = new ArrayList<>();
        List<String[]> tmpRes = new ArrayList<>();

        // 加入初始状态
        state.push(0);

        exprs.add(new String[]{"$", "-1"});

        int j = 0;
        // 对每一个表达式
        while (j < exprs.size()) {
            String expr = exprs.get(j)[0];
            int line = Integer.parseInt(exprs.get(j)[1]);

            // 拿到当前应该执行的动作
            int curStateIdx = state.peek();
            Action cur = this.actionTable.get(curStateIdx).get(expr);

            if (cur == null) {
                System.out.println("语法错误，第" + line + "行，缺少\"" + ";" + "\"");
                // 加入分号
                exprs.add(j, new String[]{";", String.valueOf(line)});
                continue;
            }

            switch (cur.action) {
                case REDUCE: {
                    Statement reduceStmt = cur.statement;

                    // 弹出符号
                    for (int k = 0; k < reduceStmt.right.size(); k++) {
                        // 坑，空串的时候不能弹出
                        if (!reduceStmt.right.get(k).equals("E")) {
                            symbol.pop();
                            state.pop();
                        }
                    }

                    // 压入新的符号
                    symbol.push(reduceStmt.left);
                    state.push(this.gotoTable.get(state.peek()).get(reduceStmt.left));

                    // 替换curLine的最后一个匹配符，并保存结果
                    List<String> rev = new ArrayList<>(symbol);
                    Collections.reverse(rev);
                    StringBuffer tmp = new StringBuffer(String.join(" ", rev));
                    tmpRes.add(new String[]{tmp.toString(), String.valueOf(j)});

                    // 重置输入
                    break;
                }
                case SHIFT: {
                    symbol.push(expr);
                    state.push(cur.state);
                    j++;
                    break;
                }
                case ACCEPT: {
                    //分析完成
                    j++;
                    break;
                }
                default:
            }
        }

        // 将修复后的句子放到res里
        for (String[] str : tmpRes) {
            StringBuffer tmp = new StringBuffer(str[0]);
            for (int k = Integer.parseInt(str[1]); k < exprs.size() - 1; k++) {
                tmp.append(" " + exprs.get(k)[0]);
            }
            res.add(tmp.toString());
        }

        StringBuffer tmp = new StringBuffer();
        tmp.append(exprs.get(0)[0]);
        for (int k = 1; k < exprs.size() - 1; k++) {
            tmp.append(" " + exprs.get(k)[0]);
        }
        res.add(0, tmp.toString());

        print(res);
    }

    public void print(List<String> res) {
        for (int i = res.size() - 1; i > 0; i--) {
            System.out.println(res.get(i) + " => ");
        }
        System.out.println(res.get(0));
    }

    /**
     * 根据Carnonical构造parsing table
     * @return parsing table
     */
    private Map<Integer, Map<String, Action>> generateTable(Map<Set<Item>, Integer> carnonical, Map<Integer, Map<String, Integer>> gotoTable) throws Exception {
        Map<Integer, Map<String, Action>> table = new HashMap<>();
        for (int i = 0; i < carnonical.size(); i++) {
            table.put(i, new HashMap<>());
        }

        for (Map.Entry<Set<Item>, Integer> entry : carnonical.entrySet()) {
            Set<Item> curState = entry.getKey();
            int stateIdx = entry.getValue();

            // 对于当前集合里的每个式子
            for (Item item : curState) {
                String next = item.getNext();
                // 判断文法中是否存在冲突
                boolean hasConflict = false;

                // 如果式子已经分析到最后
                if (next == null) {
                    // 如果是开始符
                    if (item.statement.left.equals(Constant.begin + "'")) {
                        // 这里lookAhead应该为$
                        hasConflict |= table.get(stateIdx).put(item.lookAhead, new Action(Constant.ActionState.ACCEPT)) != null;
                    }
                    else {
                        hasConflict |= table.get(stateIdx).put(item.lookAhead, new Action(Constant.ActionState.REDUCE, item.statement)) != null;
                    }
                }
                else {
                    // 如果是终结符
                    if (terminal.contains(next)) {
                        int nextStateIdx = gotoTable.get(stateIdx).get(next);
                        hasConflict |= table.get(stateIdx).put(next, new Action(Constant.ActionState.SHIFT, nextStateIdx)) != null;
                    }
                }

                // 如果在构建表项时产生了冲突
//                if (hasConflict) {
//                    throw new Exception("该文法不是LL(1)文法");
//                }
            }
        }

        return table;
    }

    /**
     * 构造文法的Carnonical
     * @param statements 输入的文法
     * @param gotoTable 需要初始化state之间的goto关系
     * @return 所有的状态
     */
    private Map<Set<Item>, Integer>  generateItems(List<Statement> statements, Map<Integer, Map<String, Integer>> gotoTable) {
        Map<Set<Item>, Integer> res = new HashMap<>();
        Map<Set<Item>, Integer> tmp = new HashMap<>();

        Set<Item> i0 = new HashSet<>();
        int cnt = 0;
        i0.add(new Item(statements.get(0), 0, "$"));

        // 初始化I0
        i0 = closure(i0, statements);
        res.put(i0, cnt++);
        tmp.put(i0, res.get(i0));

        while (true) {
            boolean addNew = false;
            // 对于每个项集I
            for (Set<Item> items : res.keySet()) {
                // 对于每个符号
                for (String term : terminal) {
                    Set<Item> goSet = go(items, term);
                    if (goSet.size() > 0) {

                        // 如果是新的set
                        if (!tmp.containsKey(goSet)) {
                            tmp.put(goSet, cnt++);
                            addNew = true;
                        }

                        // 更新gotoTable：当前state输入term后转移到的state
                        gotoTable.computeIfAbsent(res.get(items), key -> new HashMap<>()).put(term, tmp.get(goSet));
                    }
                }

                for (String term : nonTerminal) {
                    Set<Item> goSet = go(items, term);
                    if (goSet.size() > 0) {
                        // 如果是新的set
                        if (!tmp.containsKey(goSet)) {
                            tmp.put(goSet, cnt++);
                            addNew = true;
                        }

                        // 更新gotoTable：当前state输入term后转移到的state
                        gotoTable.computeIfAbsent(res.get(items), key -> new HashMap<>()).put(term, tmp.get(goSet));
                    }
                }
            }

            res.putAll(tmp);

            // 没有加入新的项
            if (!addNew) {
                break;
            }
        }

        return res;
    }

    /**
     * 计算项集所能GOTO到的下一个项集
     * @param items
     * @param input
     * @return
     */
    private Set<Item> go(Set<Item> items, String input) {
        Set<Item> res = new HashSet<>();

        for (Item item : items) {
            // 如果下一项和input对上了
            if (input.equals(item.getNext())) {
                res.add(new Item(item.statement, item.dotIdx + 1, item.lookAhead));
            }
        }

        return closure(res, this.statements);
    }

    /**
     * 根据项集计算其闭包
     * @param items item集合
     * @param statements 增广文法
     * @return 在items上做原地修改
     */
    private Set<Item> closure(Set<Item> items, List<Statement> statements) {
        Set<Item> tmp = new HashSet<>();
        tmp.addAll(items);

        while (true) {
            boolean addNew = false;
            for (Item item : items) {
                // 获得当前item的下一个待匹配字符
                String cur = item.getNext();

                // 如果是非终结符
                if (nonTerminal.contains(cur)) {
                    for (Statement statement : statements) {
                        // 如果是以cur开头的式子
                        if (statement.left.equals(cur)) {
                            Set<String> first = getFirst(item.getBehind());

                            for (String str : first) {
                                if ("E".equals(str)) {
                                    continue;
                                }
                                Item addItem = new Item(statement, 0, str);
                                if (!tmp.contains(addItem)) {
                                    tmp.add(addItem);
                                    addNew = true;
                                }
                            }
                        }
                    }
                }
            }

            items.addAll(tmp);

            // 不能再加入新的项
            if (!addNew) {
                break;
            }
        }

        return items;
    }

    /**
     * 在computeFirst函数的基础上，给定一个新的表达式，计算其first
     * @return 表达式的first集合
     */
    private Set<String> getFirst(List<String> right) {
        Set<String> res = new HashSet<>();
        String rightAll = String.join(" ", right);

        for (String cur : right) {
            // 更新rightAll的first集合
            first.computeIfAbsent(rightAll, key -> new HashSet<>()).addAll(first.get(cur));

            // 如果不含空串，退出循环
            if (!first.get(cur).contains(Constant.empty)) {
                break;
            }
        }
        res.addAll(first.get(rightAll));

        return res;
    }

    /**
     * 根据增广文法计算first函数
     * @param statements 增广文法
     */
    private Map<String, Set<String>> computeFirst(List<Statement> statements) {
        Map<String, Set<String>> first = new HashMap<>();

        // 将所有终结符加入first集合
        for (String term : terminal) {
            first.computeIfAbsent(term, key -> new HashSet<>()).add(term);
        }
        // 将所有非终结符加入first集合
        for (String term : nonTerminal) {
            first.computeIfAbsent(term, key -> new HashSet<>());
        }

        while (true) {
            boolean addNew = false;

            // 遍历每个产生式
            for (Statement statement : statements) {
                String left = statement.left;
                List<String> right = statement.right;
                String rightAll = String.join(" ", right);

                // 简单情况：如果等式右边只有一个符号
                if (right.size() == 1) {
                    // 把该符号对应的first全部加入
                    addNew |= first.get(left).addAll(first.get(right.get(0)));
                    continue;
                }

                for (String cur : right) {
                    // 把该符号对应的first全部加入
                    addNew |= first.get(left).addAll(first.get(cur));

                    // 更新rightAll的first集合
                    first.computeIfAbsent(rightAll, key -> new HashSet<>()).addAll(first.get(cur));

                    // 如果不含空串，退出循环
                    if (!first.get(cur).contains(Constant.empty)) {
                        break;
                    }
                }
            }

            // 如果本轮没有增加新的符号
            if (!addNew) {
                break;
            }
        }

        return first;
    }

    /**
     * 处理输入的文法，转化为Statement数据结构
     * @return 处理好的表达式
     */
    private List<Statement> generateStatement() {
        List<Statement> statement = new ArrayList<>();

        for (String rawStatement : Constant.ruleString) {
            // 拆分左边和右边
            int idx = rawStatement.indexOf("->");
            String left = rawStatement.substring(0, idx).trim();
            String[] rightCombination = rawStatement.substring(idx + 2).split("\\|");

            // 对于右边的每一个表达式，拆分成由空格分隔的组合
            for (String eachCombination : rightCombination) {
                String[] tmp = eachCombination.split("\\s+");
                List<String> right = new ArrayList<>();

                for (String cur : tmp) {
                    // 避免有空串
                    if (cur.length() > 0 && !"".equals(cur.trim())) {
                        right.add(cur.trim());
                    }
                }

                statement.add(new Statement(left, right));
            }
        }

        return statement;
    }

    private final Set<String> nonTerminal = Arrays.stream(Constant.nonTerminalString).collect(Collectors.toSet());
    private final Set<String> terminal = Arrays.stream(Constant.terminalString).collect(Collectors.toSet());
}