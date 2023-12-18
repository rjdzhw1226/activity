package com.activity.pojo;

import java.io.Serializable;
import java.util.Date;

public class Evection implements Serializable {
  private Long id;
  private Double days;
  private String Name;
  private Date start;
  private Date end;
  private String reason;
  private String destation;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Double getDays() {
    return days;
  }

  public void setDays(Double days) {
    this.days = days;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getDestation() {
    return destation;
  }

  public void setDestation(String destation) {
    this.destation = destation;
  }
}
