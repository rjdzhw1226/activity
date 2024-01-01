package com.activity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessEvent {
  private String activityId;
  private String activityName;
  private String assignee;
  private String comment;
  private String approvalStatus;

}
