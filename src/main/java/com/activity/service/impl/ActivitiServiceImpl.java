package com.activity.service.impl;

import com.activity.mapper.ActivitiMapper;
import com.activity.pojo.*;
import com.activity.response.ProcessEnd;
import com.activity.service.ActivitiService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
  @Override
  public Map<String, List<menuQ>> queryMenu(int i) {
    Queue<menuQ> queue = new LinkedList<>();
    List<menuVo> menuVoList = new ArrayList<>();
//    List<menu> menus = mapper.queryMenu(0);
    List<menuQ> menus = mapper.queryMenuAll();
    if(menus != null && menus.size() > 0){
      queue.addAll(menus.stream().filter(e -> e.getParent_id() == 0).collect(Collectors.toList()));
      while (!queue.isEmpty()){
        menuQ menu = queue.poll();
        menuVo me = new menuVo();
        me.setId(menu.getId());
        me.setMenuSub(new ArrayList<>());
      }
    }
    return null;
  }
  @Override
  public List<Menu> getMenuTree() {
    List<Menu> allMenus = mapper.getAllMenus();
    Map<Integer, Menu> menuMap = new HashMap<>();

    for (Menu menu : allMenus) {
      menuMap.put(menu.getId(), menu);
    }

    for (Menu menu : allMenus) {
      if (menu.getParentId() != null) {
        Menu parent = menuMap.get(menu.getParentId());
        if (parent != null) {
          parent.getChildren().add(menu);
        }
      }
    }

    List<Menu> rootMenus = new ArrayList<>();
    for (Menu menu : allMenus) {
      if (menu.getParentId() == 0) {
        rootMenus.add(menu);
      }
    }

    return rootMenus;
  }

  @Override
  public List<user> queryUsers() {
    return mapper.queryUsers();
  }
}
