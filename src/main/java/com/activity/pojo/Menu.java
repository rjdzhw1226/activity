package com.activity.pojo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private int id;
    private String label;
    private Integer parentId;
    private List<Menu> children = new ArrayList<>();

    // Getters and Setters

  public Menu() {
  }

  public Menu(int id, String label, Integer parentId, List<Menu> children) {
    this.id = id;
    this.label = label;
    this.parentId = parentId;
    this.children = children;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  public void setChildren(List<Menu> children) {
    this.children = children;
  }

  public int getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public Integer getParentId() {
    return parentId;
  }

  public List<Menu> getChildren() {
    return children;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
      .append("id", id)
      .append("label", label)
      .append("parentId", parentId)
      .append("children", children)
      .toString();
  }
}
