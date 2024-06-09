package com.activity.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class menuVo implements Serializable {

  private static final long serialVersionUID = -309747927898744003L;
  private int id;
  private String label;
  private List<menuVo> children;

  public menuVo() {
  }

  public menuVo(int id, String label, List<menuVo> menuSub) {
    this.id = id;
    this.label = label;
    this.children = new ArrayList<>(menuSub);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public List<menuVo> getMenuSub() {
    return children;
  }

  public void setMenuSub(List<menuVo> menuSub) {
    this.children = new ArrayList<>(menuSub);
  }
}
