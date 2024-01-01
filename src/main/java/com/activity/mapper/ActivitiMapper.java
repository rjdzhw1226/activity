package com.activity.mapper;

import com.activity.pojo.ProcessEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface ActivitiMapper {

  @Insert("insert into sys_act_mid (act_model_id, act_source_id, act_img_id, process_id) values (#{act_model_id}, (select act_re_model.EDITOR_SOURCE_VALUE_ID_ as act_source_id from act_re_model where ID_ = #{act_model_id}),(select act_re_model.EDITOR_SOURCE_EXTRA_VALUE_ID_ as act_img_id from act_re_model where ID_ = #{act_model_id}),#{act_process_id})")
  public int insertActMid(String act_model_id, String act_process_id);

  @Select("select ht.ID_ as activityId, " +
          "ht.NAME_ as activityName, " +
          "IFNULL(IFNULL(hc.USER_ID_, ht.ASSIGNEE_), '暂未签收') as assignee, " +
          "CONVERT(hc.FULL_MSG_ USING utf8) as comment, " +
          "hc.TIME_ as endTime,IF(ht.END_TIME_ IS NULL, '审批中', '已审批') as approvalStatus " +
          "FROM act_hi_taskinst ht LEFT JOIN act_hi_comment hc ON ht.ID_ = hc.TASK_ID_ WHERE ht.PROC_INST_ID_ = #{instanceId} AND ht.TASK_DEF_KEY_ = #{activityId} ORDER BY(ht.ID_ + 0) DESC")
  public ProcessEvent queryCurEvent(String instanceId, String activityId);
}
