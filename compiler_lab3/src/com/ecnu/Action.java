package com.ecnu;

/**
 * Parsing Table中的项
 */
class Action {
    // 对应的操作
    Constant.ActionState action;
    // 如果是Reduce，对应的表达式
    Statement statement;
    // 如果是Shift，对应的状态
    int state;

    public Action(Constant.ActionState action) {
        assert action == Constant.ActionState.ACCEPT;

        this.action = action;
        this.state = -1;
    }

    public Action(Constant.ActionState action, Statement statement) {
        assert action == Constant.ActionState.REDUCE;

        this.action = action;
        this.statement = statement;
        this.state = -1;
    }

    public Action(Constant.ActionState action, int state) {
        assert action == Constant.ActionState.SHIFT;

        this.action = action;
        this.state = state;
    }
}

