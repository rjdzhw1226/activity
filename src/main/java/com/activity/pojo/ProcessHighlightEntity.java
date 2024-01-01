package com.activity.pojo;

import lombok.Data;

import java.util.Set;

@Data
public class ProcessHighlightEntity {

    /**
     * 当前正执行节点id
     */
    private Set<String> activeActivityIds;

    /**
     * 已执行节点id
     */
    private Set<String> executedActivityIds;

    /**
     * 高亮线id（流程已走过的线）
     */
    private Set<String> highlightedFlowIds;

    /**
     * 流程xml文件 字符串
     */
    private String modelXml;

    /**
     * 流程名称
     */
    private String modelName;
}
