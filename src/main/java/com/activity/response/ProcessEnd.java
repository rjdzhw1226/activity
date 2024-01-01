package com.activity.response;

import com.activity.pojo.ProcessEvent;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProcessEnd {
  private Map<String, ProcessEvent> map;
  private List<ProcessEvent> list;
}
