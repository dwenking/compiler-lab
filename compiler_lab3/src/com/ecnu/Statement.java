package com.ecnu;

import java.util.List;

/**
 * 文法中的表达式
 */
public class Statement {
    // 表达式左侧
    String left;
    // 表达式右侧
    List<String> right;

    public Statement(String left, List<String> right) {
        this.left = left;
        this.right = right;
    }

    public void print() {
        System.out.println(this.left + " => \n" + this.right);
    }
}