package com.activity.controller;

import com.activity.pojo.ActMid;
import com.activity.pojo.Evection;
import com.activity.request.ProcessStart;
import com.activity.service.ActivitiAutoService;
import com.activity.service.ActivitiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@RestController
@RequestMapping("/activiti")
public class ActivitiController {

    /**
     * 创建模型
     */
    @RequestMapping("/create")
    public void create(HttpServletRequest request, HttpServletResponse response) {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

            RepositoryService repositoryService = processEngine.getRepositoryService();

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, "hello1111");
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            String description = "hello1111";
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName("hello1111");
            modelData.setKey("12313123");

            //保存模型
            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
//            System.out.println(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
            response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
        } catch (Exception e) {
            System.out.println("创建模型失败：");
        }
    }

    @RequestMapping("/edit/{modelId}")
    public void edit(@PathVariable("modelId") String modelId, HttpServletRequest request, HttpServletResponse response){
      try {
        response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelId);
      } catch (IOException e) {
        throw new RuntimeException("编辑失败：" + e);
      }

    }


    @Resource
    private RepositoryService repositoryService;

    @RequestMapping("/query")
    @ResponseBody
    public List<Model> queryAllModels(){
//      ModelQuery modelQuery = repositoryService.createModelQuery().notDeployed().latestVersion().orderByLastUpdateTime().desc();
      ModelQuery modelQuery = repositoryService.createModelQuery().orderByCreateTime().desc();
      return modelQuery.list();
    }

    @Resource
    private ActivitiAutoService activitiAutoService;

  /**
   * 根据Model部署流程
   */
  @RequestMapping(value = "/deploy/{modelId}")
  @ResponseBody
  public String deploy(@PathVariable("modelId") String modelId) {
    return activitiAutoService.deployment(modelId);
  }

  /**
   * 导出model对象为指定类型
   *
   * @param modelId 模型ID
   * //@param type    导出文件类型(bpmn\json)
   */
  @RequestMapping(value = "/export/{modelId}")
  public void export(@PathVariable("modelId") String modelId,
                     HttpServletResponse response) {
    try {
      Model modelData = repositoryService.getModel(modelId);
      BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
      byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
      String jsonModel = new String(bytes,"UTF-8");
      StringBuffer jsonBuff = new StringBuffer();
      jsonBuff.append("[").append(jsonModel).append(",").append(modelData.getMetaInfo()).append("]");

      JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
      BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
      //ByteArrayInputStream in = new ByteArrayInputStream(jsonBuff.toString().getBytes("UTF-8"));

      BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
      byte[] exportBytes = xmlConverter.convertToXML(bpmnModel,"UTF-8");

      String XML =  new String(exportBytes,"UTF-8");
      String filename = bpmnModel.getMainProcess().getId() + ".bpmn";

      ByteArrayInputStream in = new ByteArrayInputStream(XML.getBytes("UTF-8"));
      response.setHeader("Content-Disposition", "attachment; filename="+ filename);

      IOUtils.copy(in, response.getOutputStream());

      response.flushBuffer();
    // FileUtil.exportFile(response, in,filename);
    } catch (Exception e) {
      System.out.println("导出model的xml文件失败！" + e);
    }
  }

  @Resource
  private RuntimeService runtimeService;

  /**
   * 流程启动
   */
  @RequestMapping("/start/evection")
  @ResponseBody
  public String start(@RequestBody ProcessStart processStart){
    //带着username传过来查角色和对应审批人 然后启动就ok
    // ？事务业务和流程的问题
    //一张配置表 存对应的公司下对应的科室 对应的人的审核人 一张公司表和科室表
    Map<String,Object> map = new HashMap<>();
    Evection eve = new Evection();
    eve.setDays(4d);
    map.put("evection", eve);
    //创建人 这里应该是传过来的username
    map.put("assigness0", processStart.getUsername());
    //后面是对应审批人
    map.put("assigness1", "lisi");
    map.put("assigness2", "wangwu");
    map.put("assigness3", "zhaoliu");
    //这的s1关联自己的业务表 达到分离流程和业务的功能
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process","", map);
    System.out.println(instance.getProcessDefinitionId());
    System.out.println(instance.getActivityId());
    return "启动成功，启动ID=" + instance.getId();
  }

  @RequestMapping("/start")
  @ResponseBody
  public String startDemo(){
    //带着username传过来查角色和对应审批人 然后启动就ok
    // ？事务业务和流程的问题
    //一张配置表 存对应的公司下对应的科室 对应的人的审核人 一张公司表和科室表
    Map<String,Object> map = new HashMap<>();
    Evection eve = new Evection();
    eve.setDays(4d);
    map.put("evection", eve);
    //创建人 这里应该是传过来的username
    map.put("assigness0", "zhangsan");
    //后面是对应审批人
    map.put("assigness1", "lisi");
    map.put("assigness2", "wangwu");
    map.put("assigness3", "zhaoliu");
    //这的s1关联自己的业务表 达到分离流程和业务的功能
//    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process","", map);
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process", map);
    System.out.println(instance.getProcessDefinitionId());
    System.out.println(instance.getActivityId());
    return "启动成功，启动ID=" + instance.getId();
  }

  @RequestMapping(value = "/close/{id}", method = RequestMethod.GET)
  @ResponseBody
  public String close(@PathVariable(value = "id") String id, HttpServletResponse response) {
    Model model = repositoryService.getModel(id);
    if (model != null) {
        repositoryService.deleteModel(id);
        return "刪除成功！";
    } else {
        return "未找到模型，删除失败！";
    }
//    if (model != null && !model.hasEditorSourceExtra()) {
//      repositoryService.deleteModel(id);
//    }
  }

  @RequestMapping(value = "/showmodelpicture/{modelId}")
  public void showModelPicture(HttpServletResponse response ,@PathVariable(value = "modelId") String modelId)throws Exception{
    Model modelData = repositoryService.getModel(modelId);
    ObjectNode modelNode = null;
    try {
      modelNode = (ObjectNode) new ObjectMapper()
        .readTree(repositoryService.getModelEditorSource(modelData.getId()));
    } catch (IOException e) {
      e.printStackTrace();
    }
    BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
    ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
    InputStream inputStream = processDiagramGenerator.generateDiagram(model, "png", Collections.<String>emptyList(), Collections.<String>emptyList(), "WenQuanYi Micro Hei", "WenQuanYi Micro Hei", "WenQuanYi Micro Hei", null, 1.0d);
    OutputStream out = response.getOutputStream();
    for (int b = -1; (b = inputStream.read()) != -1; ) {
      out.write(b);
    }
    out.close();
    inputStream.close();
  }



  @Resource
  private TaskService taskService;
  /**
   * 查询自己处理的流程
   */
  public void queryMine(){
    Task task = taskService.createTaskQuery()
      .processDefinitionKey("evection")
      .taskAssignee("jack")
      .singleResult();
  }

}
