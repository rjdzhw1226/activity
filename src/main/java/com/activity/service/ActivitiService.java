package com.activity.service;

import com.activity.pojo.ActMid;
import org.activiti.bpmn.model.BpmnModel;

public interface ActivitiService {
  public int insertActMid(String modelId, BpmnModel model);

}
