package com.activity.service.impl;

import com.activity.mapper.ActivitiMapper;
import com.activity.pojo.ActMid;
import com.activity.pojo.ProcessEvent;
import com.activity.response.ProcessEnd;
import com.activity.service.ActivitiService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivitiServiceImpl implements ActivitiService {
  @Resource
  private ActivitiMapper mapper;

  @Override
  @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
  public int insertActMid(String modelId, BpmnModel model){
    List<String> processIds = new ArrayList<>();
    for (Process process : model.getProcesses()) {
      processIds.add(process.getId());
    }
    return mapper.insertActMid(String.valueOf(modelId), processIds.get(0));
  }

  @Override
  public ProcessEnd queryCurEvent(String instanceId, String activityId) {
    Map<String, ProcessEvent> map = new HashMap<>();
    ProcessEvent processEvent = mapper.queryCurEvent(instanceId, activityId);
    map.put(activityId, processEvent);
    ProcessEnd processEnd = new ProcessEnd();
    processEnd.setMap(map);
    System.out.println(processEvent);
    return processEnd;
  }
}
