package com.activity.service.impl;

import com.activity.pojo.ActMid;
import com.activity.service.ActivitiAutoService;
import com.activity.service.ActivitiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ActivitiAutoServiceImpl implements ActivitiAutoService {

  @Resource
  private ActivitiService activitiService;

  @Resource
  private RepositoryService repositoryService;

  @Override
  @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
  public String deployment(String modelId) {
    try {
      Model modelData = repositoryService.getModel(modelId);

      ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
      BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
      byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
      InputStream input = new ByteArrayInputStream(bpmnBytes);
      String processName = modelData.getName() + ".bpmn20.xml";

      int i = activitiService.insertActMid(modelId, model);

      if(i != 1){
        throw new RuntimeException("中间表插入失败！");
      }

      Deployment deployment = repositoryService.createDeployment().addInputStream(processName, input).deploy();

      log.info("部署成功，部署ID=" + deployment.getId());
      return "部署ID=" + deployment.getId();
    } catch (Exception e) {
      e.printStackTrace();
      log.info("根据模型部署流程失败：modelId="+ modelId);
      return "modelId=" + modelId;
    }
  }
}
