package com.ximi.huiaiagent.agent.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct 模型
 *
 * @author Ximi
 * @since 2023/9/5
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent{

    /**
     * 处理当前状态并决定下一步行动
     * @return
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     * @return
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            boolean think = think();
            if (!think) {
                return "思考完成 - 无需行动";
            }
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "执行行动时发生错误：" + e.getMessage();
        }
    }
}
