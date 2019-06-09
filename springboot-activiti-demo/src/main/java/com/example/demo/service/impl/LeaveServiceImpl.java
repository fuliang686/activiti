package com.example.demo.service.impl;

import com.example.demo.domain.LeaveApply;
import com.example.demo.mapper.LeaveApplyMapper;
import com.example.demo.service.LeaveService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 5)
@Service
public class LeaveServiceImpl implements LeaveService {
    @Autowired
    LeaveApplyMapper leavemapper;
    @Autowired
    IdentityService identityservice;
    @Autowired
    RuntimeService runtimeservice;
    @Autowired
    TaskService taskservice;

    public ProcessInstance startWorkflow(LeaveApply apply, String userId, Map<String, Object> variables) {
        apply.setApply_time(new Date().toString());
        apply.setUser_id(userId);
        leavemapper.save(apply);
        String businessKey = String.valueOf(apply.getId());//使用leaveapply表的主键作为businesskey,连接业务数据和流程数据
        identityservice.setAuthenticatedUserId(userId);
        ProcessInstance instance = runtimeservice.startProcessInstanceByKey("leave", businessKey, variables);
        System.out.println(businessKey);
        String instanceId = instance.getId();
        apply.setProcess_instance_id(instanceId);
        leavemapper.update(apply);
        return instance;
    }

    @Override
    public List<LeaveApply> getDeptTaskList(String candidateGroupName) {
        List<LeaveApply> results = new ArrayList<LeaveApply>();
        List<Task> tasks = taskservice.createTaskQuery().taskCandidateGroup(candidateGroupName).list();
        for (Task task : tasks) {
            String instanceId = task.getProcessInstanceId();
            ProcessInstance ins = runtimeservice.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
            String businessKey = ins.getBusinessKey();
            LeaveApply a = leavemapper.get(Integer.parseInt(businessKey));
            a.setTask(task);
            results.add(a);
        }
        return results;
    }

    @Override
    public void completeReportBack(String taskId, String real_start_time, String real_end_time) {
        Task task = taskservice.createTaskQuery().taskId(taskId).singleResult();
        String instanceId = task.getProcessInstanceId();
        ProcessInstance ins = runtimeservice.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        String businessKey = ins.getBusinessKey();
        LeaveApply a = leavemapper.get(Integer.parseInt(businessKey));
        a.setReality_start_time(real_start_time);
        a.setReality_end_time(real_end_time);
        leavemapper.update(a);
        taskservice.complete(taskId);
    }

    @Override
    public void updateComplete(String taskId, LeaveApply leave, String reapply) {
        Task task = taskservice.createTaskQuery().taskId(taskId).singleResult();
        String instanceId = task.getProcessInstanceId();
        ProcessInstance ins = runtimeservice.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        String businessKey = ins.getBusinessKey();
        LeaveApply a = leavemapper.get(Integer.parseInt(businessKey));
        a.setLeave_type(leave.getLeave_type());
        a.setStart_time(leave.getStart_time());
        a.setEnd_time(leave.getEnd_time());
        a.setReason(leave.getReason());
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("reapply", reapply);
        if (reapply.equals("true")) {
            leavemapper.update(a);
            taskservice.complete(taskId, variables);
        } else
            taskservice.complete(taskId, variables);
    }

    @Override
    public LeaveApply getleave(int id) {
        LeaveApply leave=leavemapper.get(id);
        return leave;
    }
}
