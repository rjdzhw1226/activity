package com.activity.pojo;

public class ActMid {
  private int id;
  private int act_model_id;
  private int act_source_id;
  private int act_img_id;
  private String process_id;

  public ActMid() {
  }

  public ActMid(int act_model_id, int act_source_id, int act_img_id, String process_id) {
    this.act_model_id = act_model_id;
    this.act_source_id = act_source_id;
    this.act_img_id = act_img_id;
    this.process_id = process_id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getAct_model_id() {
    return act_model_id;
  }

  public void setAct_model_id(int act_model_id) {
    this.act_model_id = act_model_id;
  }

  public int getAct_source_id() {
    return act_source_id;
  }

  public void setAct_source_id(int act_source_id) {
    this.act_source_id = act_source_id;
  }

  public int getAct_img_id() {
    return act_img_id;
  }

  public void setAct_img_id(int act_img_id) {
    this.act_img_id = act_img_id;
  }

  public String getProcess_id() {
    return process_id;
  }

  public void setProcess_id(String process_id) {
    this.process_id = process_id;
  }

  @Override
  public String toString() {
    return "ActMid{" +
      "id=" + id +
      ", act_model_id=" + act_model_id +
      ", act_source_id=" + act_source_id +
      ", act_img_id=" + act_img_id +
      ", process_id='" + process_id + '\'' +
      '}';
  }
}
