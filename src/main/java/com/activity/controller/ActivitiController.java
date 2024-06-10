package com.activity.controller;

import cn.hutool.core.io.IoUtil;
import com.activity.pojo.*;
import com.activity.request.ProcessStart;
import com.activity.response.ProcessEnd;
import com.activity.service.ActivitiAutoService;
import com.activity.service.ActivitiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiParam;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/activiti")
public class ActivitiController {

    /**
     * 创建模型
     */
    @RequestMapping("/create")
    public void create(@RequestParam String name, @RequestParam String description, @RequestParam String key, @RequestParam String region, @RequestParam String selectId, HttpServletRequest request, HttpServletResponse response) {
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

            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, "Model：" + name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName(name);
            modelData.setKey(key);
            modelData.setCategory(region + ":" + selectId);

            //保存模型
            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
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

    @RequestMapping("/queryProcess/{def}/{defKey}")
    @ResponseBody
    public List<ProcessDefinition> queryAllProcesses(@PathVariable("def") String def, @PathVariable("defKey") String defKey){
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionId()
                .orderByProcessDefinitionKey().desc()
                .orderByProcessDefinitionVersion().desc();
        query.processDefinitionNameLike("%" + def + "%");
        query.processDefinitionKeyLike("%" + defKey + "%");
        return query.list();
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

    @RequestMapping("/completeTask/{id}")
    public void completeTask(@PathVariable("id") String id){
        taskService.complete(id);
    }

    @Resource
    private ActivitiService activitiService;

    @RequestMapping("/flow/event")
    @ResponseBody
    public ProcessEnd queryCurEvent(@RequestBody Proc proc){
        return activitiService.queryCurEvent(proc.getInstanceId(), proc.getActivityId());
    }

    @Resource
    private HistoryService historyService;

    private static Map<String, String> BPMN_XML_MAP = new ConcurrentHashMap<>();


    @RequestMapping("/flow/definite")
    @ResponseBody
    public ProcessHighlightEntity getActivitiProcessHighlight(@RequestBody Proc proc) {
        String procDefId = proc.getProcDefId();
        String instanceId = proc.getInstanceId();
        ProcessDefinition processDefinition = getProcessDefinition(procDefId, instanceId);
        procDefId = processDefinition.getId();
        BpmnModel bpmnModel = getBpmnModel(procDefId);
        List<HistoricActivityInstance> histActInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId).orderByHistoricActivityInstanceId().asc().list();
        ProcessHighlightEntity highlightEntity = getHighLightedData(bpmnModel.getMainProcess(), histActInstances);
        highlightEntity.setModelName(processDefinition.getName());
        // Map缓存，提高获取流程文件速度
        if (BPMN_XML_MAP.containsKey(procDefId)) {
            highlightEntity.setModelXml(BPMN_XML_MAP.get(procDefId));
        } else {
            InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
            try (Reader reader = new InputStreamReader(bpmnStream, StandardCharsets.UTF_8)) {
                String xmlString = IoUtil.read(reader);
                highlightEntity.setModelXml(xmlString);
                BPMN_XML_MAP.put(procDefId, xmlString);
            } catch (IOException e) {
                System.out.printf("[获取流程数据] 失败，{}", e.getMessage());
                throw new RuntimeException("获取流程数据失败，请稍后重试");
            }
        }
        return highlightEntity;
    }

    public ProcessDefinition getProcessDefinition(String procDefId, String instanceId) {
        if (StringUtils.isBlank(procDefId)) {
            if (StringUtils.isBlank(instanceId)) {
                throw new RuntimeException("流程实例id，流程定义id 两者不能都为空");
            }
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(instanceId)
                    .singleResult();
            if (processInstance == null) {
                HistoricProcessInstance histProcInst = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(instanceId)
                        .singleResult();
                if (histProcInst == null) {
                    throw new RuntimeException("查询失败，请检查传入的 instanceId 是否正确");
                }
                procDefId = histProcInst.getProcessDefinitionId();
            } else {
                procDefId = processInstance.getProcessDefinitionId();
            }
        }
        try {
            return repositoryService.getProcessDefinition(procDefId);
        } catch (ActivitiObjectNotFoundException e) {
            throw new RuntimeException("该流程属于之前流程，已删除");
        }
    }

    public BpmnModel getBpmnModel(String procDefId) {
        try {
            return repositoryService.getBpmnModel(procDefId);
        } catch (ActivitiObjectNotFoundException e) {
            throw new RuntimeException("流程定义数据不存在");
        }
    }

    private ProcessHighlightEntity getHighLightedData(Process process,
                                                      List<HistoricActivityInstance> historicActInstances) {
        ProcessHighlightEntity entity = new ProcessHighlightEntity();
        // 已执行的节点id
        Set<String> executedActivityIds = new HashSet<>();
        // 正在执行的节点id
        Set<String> activeActivityIds = new HashSet<>();
        // 高亮流程已发生流转的线id集合
        Set<String> highLightedFlowIds = new HashSet<>();
        // 全部活动节点
        List<FlowNode> historicActivityNodes = new ArrayList<>();
        // 已完成的历史活动节点
        List<HistoricActivityInstance> finishedActivityInstances = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActInstances) {
            FlowNode flowNode = (FlowNode) process.getFlowElement(historicActivityInstance.getActivityId(), true);
            historicActivityNodes.add(flowNode);
            if (historicActivityInstance.getEndTime() != null) {
                finishedActivityInstances.add(historicActivityInstance);
                executedActivityIds.add(historicActivityInstance.getActivityId());
            } else {
                activeActivityIds.add(historicActivityInstance.getActivityId());
            }
        }

        FlowNode currentFlowNode = null;
        FlowNode targetFlowNode = null;
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstances) {
            // 获得当前活动对应的节点信息及outgoingFlows信息
            currentFlowNode = (FlowNode) process.getFlowElement(currentActivityInstance.getActivityId(), true);
            List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();

            /**
             * 遍历outgoingFlows并找到已已流转的 满足如下条件认为已已流转：
             * 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
             * 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
             */
            if ("parallelGateway".equals(currentActivityInstance.getActivityType()) || "inclusiveGateway".equals(currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    targetFlowNode = (FlowNode) process.getFlowElement(sequenceFlow.getTargetRef(), true);
                    if (historicActivityNodes.contains(targetFlowNode)) {
                        highLightedFlowIds.add(sequenceFlow.getId());
                    }
                }
            } else {
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    for (HistoricActivityInstance historicActivityInstance : historicActInstances) {
                        if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("highLightedFlowId", sequenceFlow.getId());
                            map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                            tempMapList.add(map);
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(tempMapList)) {
                    // 遍历匹配的集合，取得开始时间最早的一个
                    long earliestStamp = 0L;
                    String highLightedFlowId = null;
                    for (Map<String, Object> map : tempMapList) {
                        long highLightedFlowStartTime = Long.parseLong(map.get("highLightedFlowStartTime").toString());
                        if (earliestStamp == 0 || earliestStamp >= highLightedFlowStartTime) {
                            highLightedFlowId = map.get("highLightedFlowId").toString();
                            earliestStamp = highLightedFlowStartTime;
                        }
                    }
                    highLightedFlowIds.add(highLightedFlowId);
                }
            }
        }

        entity.setActiveActivityIds(activeActivityIds);
        entity.setExecutedActivityIds(executedActivityIds);
        entity.setHighlightedFlowIds(highLightedFlowIds);

        return entity;
    }

    @GetMapping("/menu")
    @ResponseBody
    public Map<String, List<menuQ>> queryMenu(){
        return activitiService.queryMenu(0);
    }

    @GetMapping("/menu-tree")
    @ResponseBody
    public Map<String, List<Menu>>  getMenuTree() {
        List<Menu> menuTree = activitiService.getMenuTree();
        Map<String, List<Menu>> map = new HashMap<>();
        map.put("data", menuTree);
        return map;
    }

    @GetMapping("/user")
    @ResponseBody
    public Map<String, List<user>> getUser(){
        List<user> users = activitiService.queryUsers();
        Map<String, List<user>> map = new HashMap<>();
        map.put("rows", users);
        return map;
    }

    @GetMapping("/getAllUser")
    public CommonResponse getAllUser(@RequestParam(value = "pageNum", required = false,defaultValue = "1")
                                     @ApiParam(value = "页码" ,required = false)int pageNum,
                                     @RequestParam(value = "pageSize", required = false,defaultValue = "5")
                                     @ApiParam(value = "条数" ,required = false)int pageSize){
        Page<Object> pageObject = PageHelper.startPage(pageNum, pageSize);
        List<ActIdUser> userList = new ArrayList<>();
        userList.add(new ActIdUser("1",1,"R","JD","cf656779436@163.com","123456","www"));
        userList.add(new ActIdUser("2",1,"K","FG","cf656779436@163.com","123456","www"));
        userList.add(new ActIdUser("3",1,"B","WE","cf656779436@163.com","123456","www"));
        PageInfo<ActIdUser> pageInfo = new PageInfo<>(userList);
        return new CommonResponse().code("0000").data(pageInfo);
    }

    @GetMapping(value = "getAllGroup")
    public CommonResponse getAllGroup(@RequestParam(value = "pageNum", required = false,defaultValue = "1")
                                      @ApiParam(value = "页码" ,required = false)int pageNum,
                                      @RequestParam(value = "pageSize", required = false,defaultValue = "5")
                                      @ApiParam(value = "条数" ,required = false)int pageSize){
        Page<Object> pageObject = PageHelper.startPage(pageNum, pageSize);
        PageInfo<ActIdUser> pageInfo = new PageInfo<>(new ArrayList<>());
        return new CommonResponse().code("0000").data(pageInfo);
    }

}
