package com.activity.service.impl;

import com.activity.mapper.ActivitiMapper;
import com.activity.pojo.ActMid;
import com.activity.service.ActivitiService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
}
