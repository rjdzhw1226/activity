package com.activity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class taskVo {
  String id;
  String name;
  String assignee;
  String processInstanceId;
  String processDefinitionId;
  Date createTime;
}
