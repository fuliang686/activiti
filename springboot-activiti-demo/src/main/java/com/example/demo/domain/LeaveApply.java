package com.example.demo.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.activiti.engine.task.Task;

import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("请假表")
public class LeaveApply implements Serializable{
	int id;
	String process_instance_id;
	String user_id;
	@ApiModelProperty(value = "请假起始时间",name = "user_id",dataType = "String",required = true)
	String start_time;
	@ApiModelProperty(value = "请假结束时间",name = "user_id",dataType = "String",required = true)
	String end_time;
	@ApiModelProperty(value = "请假类型",name = "user_id",dataType = "String",required = true)
	String leave_type;
	@ApiModelProperty(value = "请假原因",name = "user_id",dataType = "String",required = true)
	String reason;
	String apply_time;
	String reality_start_time;
	String reality_end_time;
	Task task;
}
