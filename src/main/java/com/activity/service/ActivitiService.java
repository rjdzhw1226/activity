package com.activity.service;

import com.activity.pojo.ActMid;
import com.activity.response.ProcessEnd;
import org.activiti.bpmn.model.BpmnModel;

public interface ActivitiService {
  public int insertActMid(String modelId, BpmnModel model);

  public ProcessEnd queryCurEvent(String instanceId, String activityId);

}
