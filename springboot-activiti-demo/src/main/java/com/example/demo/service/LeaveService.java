package com.example.demo.service;

import com.example.demo.domain.LeaveApply;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Map;


public interface LeaveService {
    public ProcessInstance startWorkflow(LeaveApply apply, String userId, Map<String, Object> variables);

    List<LeaveApply> getDeptTaskList(String candidateGroupName);

    void completeReportBack(String taskId, String real_start_time, String real_end_time);

    void updateComplete(String taskId, LeaveApply leave, String reapply);

    LeaveApply getleave(int id);
}
