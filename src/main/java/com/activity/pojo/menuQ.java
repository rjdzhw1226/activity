package com.activity.pojo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class menuQ implements Serializable {

  private static final long serialVersionUID = -338842558264737303L;
  private int id;
  private String label;
  private int parent_id;

  public menuQ() {
  }

  public menuQ(String label, menuQ childMenu) {
    this.label = label;
  }

  public menuQ(int id, String label, int parent_id, menuQ childMenu) {
    this.id = id;
    this.label = label;
    this.parent_id = parent_id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getParent_id() {
    return parent_id;
  }

  public void setParent_id(int parent_id) {
    this.parent_id = parent_id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
      .append("label", label)
      .toString();
  }
}
