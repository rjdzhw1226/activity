package com.activity.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class DefinitionListVo {
    /**
     * 流程id
     */
    private String id;

    /**
     * 部署id
     */
    private String deploymentId;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程key
     */
    private String key;

    /**
     * 版本
     */
    private int version;

    /**
     * 是否挂起状态
     */
    private boolean isSuspended;

    /**
     * 部署时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deploymentTime;
}
