package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.example.demo.base.Constants;
import com.example.demo.domain.LeaveApply;
import com.example.demo.domain.RunningProcess;
import com.example.demo.service.LeaveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author FL
 * @ClassName: com.example.demo.controller.LeaveController
 * @description: TODO
 * @date 2019/6/9 9:26
 */
@Controller
@Api(tags = "请假接口")
public class LeaveController {

    @Autowired
    RepositoryService rep;
    @Autowired
    RuntimeService runservice;
    @Autowired
    FormService formservice;
    @Autowired
    IdentityService identityservice;
    @Autowired
    LeaveService leaveservice;
    @Autowired
    TaskService taskservice;
    @Autowired
    HistoryService histiryservice;

    /**
     * 请假申请发起流程
     */
    @RequestMapping(value = "/startLeave", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("请假申请")
    public String start_leave(@ModelAttribute("leave") LeaveApply leave) {
        String userId = Constants.userName;
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applyUserId", userId);
        ProcessInstance ins = leaveservice.startWorkflow(leave, userId, variables);
        System.out.println("流程id" + ins.getId() + "已启动");
        return JSON.toJSONString("sucess");
    }

    /**
     * 获取部门领导/人事审批代办列表  candidateGroupName 部门经理  人事
     */
    @ApiOperation("获取部门领导/人事审批代办列表")
    @RequestMapping(value = "/deptTaskList", method = RequestMethod.POST)
    @ResponseBody
    public String deptTaskList(@RequestParam String candidateGroupName) {
        List<LeaveApply> results = leaveservice.getDeptTaskList(candidateGroupName);
        return results.toString();
    }

    /**
     * 部门经理审批
     */
    @ApiOperation("部门经理审批")
    @RequestMapping(value = "/task/deptComplete", method = RequestMethod.POST)
    @ResponseBody
    public String deptComplete(@ApiParam(name = "taskId", value = "任务id", required = true) @RequestParam("taskId") String taskId,
                               @ApiParam(name = "deptLeaderApprove", value = "true通过false拒绝", required = true)
                               @RequestParam("deptLeaderApprove") String deptLeaderApprove) {
        String userId = Constants.userName;
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("deptLeaderApprove", deptLeaderApprove);
        taskservice.claim(taskId, userId);
        taskservice.complete(taskId, variables);
        return JSON.toJSONString("success");
    }

    /**
     * 重新申请
     */
    @ApiOperation("重新申请")
    @RequestMapping(value = "/task/updateComplete", method = RequestMethod.POST)
    @ResponseBody
    public String updatecomplete(@ApiParam(name = "taskId", value = "任务id", required = true) @RequestParam("taskId") String taskId,
                                 @ApiParam(name = "reapply", value = "true通过false拒绝", required = true) @RequestParam("reapply") String reapply,
                                 @ModelAttribute("leave") LeaveApply leave) {
        leaveservice.updateComplete(taskId, leave, reapply);
        return JSON.toJSONString("success");
    }

    /**
     * 人事审批
     */
    @ApiOperation("人事审批")
    @RequestMapping(value = "/task/hrComplete", method = RequestMethod.POST)
    @ResponseBody
    public String hrComplete(@ApiParam(name = "taskId", value = "任务id", required = true) @RequestParam("taskId") String taskId,
                             @ApiParam(name = "hrApprove", value = "1通过2拒绝3驳回", required = true) @RequestParam("hrApprove") String hrApprove) {
        String userId = Constants.userName;
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("hrApprove", hrApprove);
        taskservice.claim(taskId, userId);
        taskservice.complete(taskId, variables);
        return JSON.toJSONString("success");
    }

    /**
     * 销假
     */
    @ApiOperation("销假")
    @RequestMapping(value = "/task/reportBackComplete", method = RequestMethod.POST)
    @ResponseBody
    public String reportBackComplete(@ApiParam(name = "taskId", value = "任务id", required = true) @RequestParam("taskId") String taskId) {
        String real_start_time = "2019-07-01";
        String real_end_time = "2019-07-03";
        leaveservice.completeReportBack(taskId, real_start_time, real_end_time);
        return JSON.toJSONString("success");
    }

    /**
     * 我的请假历史数据
     */
    @ApiOperation("我的请假历史数据")
    @RequestMapping(value = "/getFinishProcess", method = RequestMethod.POST)
    @ResponseBody
    public String getFinishProcess() {
        String userId = Constants.userName;
        HistoricProcessInstanceQuery process = histiryservice.createHistoricProcessInstanceQuery().
                processDefinitionKey("leave").startedBy(userId).finished();
        List<HistoricProcessInstance> info = process.list();
        List<LeaveApply> list = new ArrayList<LeaveApply>();
        for (HistoricProcessInstance history : info) {
            System.out.println("bussinessKey" + history.getBusinessKey());
            System.out.println("ProcessDefinitionId" + history.getProcessDefinitionId());
            LeaveApply apply = leaveservice.getleave(Integer.parseInt(history.getBusinessKey()));
            list.add(apply);
        }
        return list.toString();
    }

    /**
     * 请假历史数据 详情
     */
    @ApiOperation("请假历史数据-详情")
    @RequestMapping(value = "/processInfo", method = RequestMethod.POST)
    @ResponseBody
    public List<HistoricActivityInstance> processInfo(@ApiParam(name = "instanceId", value = "流程实例id", required = true)
                                                      @RequestParam("instanceId") String instanceId) {
        List<HistoricActivityInstance> his = histiryservice.createHistoricActivityInstanceQuery().
                processInstanceId(instanceId).orderByHistoricActivityInstanceStartTime().asc().list();
        return his;
    }

    /**
     * 用户发起的正在运行的请假流程
     */
    @ApiOperation("用户发起的正在运行的请假流程")
    @RequestMapping(value = "setupProcess", method = RequestMethod.POST)
    @ResponseBody
    public String setupProcess() {
        ProcessInstanceQuery query = runservice.createProcessInstanceQuery();
        String userId = Constants.userName;
        List<ProcessInstance> a = query.processDefinitionKey("leave").involvedUser(userId).list();
        List<RunningProcess> list = new ArrayList<RunningProcess>();
        for (ProcessInstance p : a) {
            RunningProcess process = new RunningProcess();
            process.setActivityid(p.getActivityId());
            process.setBusinesskey(p.getBusinessKey());
            process.setExecutionid(p.getId());
            process.setProcessInstanceid(p.getProcessInstanceId());
            LeaveApply l = leaveservice.getleave(Integer.parseInt(p.getBusinessKey()));
            if (l.getUser_id().equals(userId))
                list.add(process);
            else
                continue;
        }
        return list.toString();
    }

    /**
     * 用户参与的正在运行的请假流程
     */
    @ApiOperation("用户参与的正在运行的请假流程")
    @RequestMapping(value = "involvedProcess", method = RequestMethod.POST)
    @ResponseBody
    public String involvedProcess() {
        ProcessInstanceQuery query = runservice.createProcessInstanceQuery();
        String userId = Constants.userName;
        List<ProcessInstance> a = query.processDefinitionKey("leave").involvedUser(userId).list();
        List<RunningProcess> list = new ArrayList<RunningProcess>();
        for (ProcessInstance p : a) {
            RunningProcess process = new RunningProcess();
            process.setActivityid(p.getActivityId());
            process.setBusinesskey(p.getBusinessKey());
            process.setExecutionid(p.getId());
            process.setProcessInstanceid(p.getProcessInstanceId());
            list.add(process);
        }
        return list.toString();
    }

}
