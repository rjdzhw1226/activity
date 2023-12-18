package com.activity.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ActivitiMapper {

  @Insert("insert into sys_act_mid (act_model_id, act_source_id, act_img_id, process_id) values (#{act_model_id}, (select act_re_model.EDITOR_SOURCE_VALUE_ID_ as act_source_id from act_re_model where ID_ = #{act_model_id}),(select act_re_model.EDITOR_SOURCE_EXTRA_VALUE_ID_ as act_img_id from act_re_model where ID_ = #{act_model_id}),#{act_process_id})")
  public int insertActMid(String act_model_id, String act_process_id);
}
