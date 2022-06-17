package com.ecnu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 语法分析中的项
 */
class Item {
    Statement statement;
    String lookAhead;
    // dot所在的位置
    int dotIdx;

    public Item(Statement statement, int dotIdx, String lookAhead) {
        this.statement = statement;
        this.dotIdx = dotIdx;
        this.lookAhead = lookAhead;
        while (this.dotIdx < statement.right.size() && "E".equals(statement.right.get(dotIdx))) {
            this.dotIdx++;
        }
    }

    /**
     * 获得下一个待分析符号
     * @return 待分析符号
     */
    public String getNext() {
        // 如果是空串直接跳过
        while (this.dotIdx < statement.right.size() && "E".equals(statement.right.get(dotIdx))) {
            this.dotIdx++;
        }
        if (dotIdx >= statement.right.size()) {
            return null;
        }

        return statement.right.get(dotIdx);
    }

    /**
     * 分析时，拿到item待分析项后面的所有符号
     * @return 待分析项后面的所有符号
     */
    public List<String> getBehind() {
        List<String> res = new ArrayList<>();
        for (int i = dotIdx + 1; i < statement.right.size(); i++) {
            res.add(statement.right.get(i));
        }
        res.add(lookAhead);

        return res;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.statement, this.lookAhead, this.dotIdx);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Item)) {
            return false;
        }
        Item item = (Item) o;
        return this.dotIdx == item.dotIdx &&
                this.lookAhead.equals(item.lookAhead) && this.statement.left.equals(item.statement.left) &&
                this.statement.right.equals(item.statement.right);
    }
}
