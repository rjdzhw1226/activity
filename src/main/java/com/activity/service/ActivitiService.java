package com.activity.service;

import com.activity.pojo.ActMid;
import com.activity.pojo.Menu;
import com.activity.pojo.menuQ;
import com.activity.pojo.user;
import com.activity.response.ProcessEnd;
import org.activiti.bpmn.model.BpmnModel;

import java.util.List;
import java.util.Map;

public interface ActivitiService {
  public int insertActMid(String modelId, BpmnModel model);

  public ProcessEnd queryCurEvent(String instanceId, String activityId);

  Map<String, List<menuQ>> queryMenu(int i);

  public List<Menu> getMenuTree();

  public List<user> queryUsers();

}
