package com.omsk.flowable.controller;

import com.omsk.flowable.domain.dto.CompleteDto;
import com.omsk.flowable.domain.dto.HolidayDto;
import com.omsk.flowable.domain.dto.QueryTaskDto;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @Author Sunjia'nan
 * @Date 2022/7/21 11:09
 * @Version 1.0
 */
@RestController
@RequestMapping("flowable")
public class FlowableController {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    // 自动部署
    @Qualifier("processEngine")
    @Autowired
    private ProcessEngine processEngine;

    // springboot 已经自动部署
    @GetMapping("deploy")
    public String deployment() {

        Deployment deploy = repositoryService.createDeployment().deploy();

        return "deployment.getId() = " + deploy.getId();
    }

    @GetMapping("query/{id}")
    public String queryDeployment(@PathParam("id") String id){

        System.out.println(id);

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(id)
                .singleResult();

        return "deployment.getId() = " + processDefinition.getId()
                + "processDefinition.getName() = " + processDefinition.getName()
                + "processDefinition.getDeploymentId() = " + processDefinition.getDeploymentId()
                + "processDefinition.getDescription() = " + processDefinition.getDescription();
    }

    @PostMapping("add")
    public String addProcess(@RequestBody HolidayDto holidayDto) {

        Map<String, Object> request = beanToMap(holidayDto.getHoliday());

        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(holidayDto.getKey(), request);

        return "流程定义的ID：" + processInstance.getProcessDefinitionId()
                + "流程实例的ID：" + processInstance.getId()
                + "当前活动的ID：" + processInstance.getActivityId();
    }

    @PostMapping("query")
    public String queryProcess(@RequestBody QueryTaskDto queryTaskVo) {


        // 根据流程和当前处理用户查询流程列表
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey(queryTaskVo.getKey())
                .taskAssignee(queryTaskVo.getAssignee())
                .list();

        for (Task task : list) {
            System.out.println("task.getProcessDefinitionId() = " + task.getProcessDefinitionId());
            System.out.println("task.getId() = " + task.getId());
            System.out.println("task.getAssignee() = " + task.getAssignee());
            System.out.println("task.getName() = " + task.getName());

            // 申请流程信息
            Map<String, Object> variables = taskService.getVariables(task.getId());
            System.out.println(variables);

        }

        return list.toArray().toString();

    }

    @PostMapping("completeTask")
    public String completeTask(@RequestBody CompleteDto completeDto) {

        String id = completeDto.getId();

        // 放置参数
        Map<String, Object> map = beanToMap(completeDto.getComplete());

        taskService.complete(id, map);


        return "success ";
    }




    /**
     * 生成进行中的流程图
     *
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    @PostMapping("queryProcessInstanceDiagram")
    public byte[] queryProcessInstanceDiagram(String processInstanceId) throws Exception {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            return null;
        }
        List<HistoricActivityInstance> historicActivityInstanceList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceId().asc().list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        // TODO 获取flow
        List<String> flows = new ArrayList<>();
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
            activityIds.add(historicActivityInstance.getActivityId());
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = diagramGenerator
                    .generateDiagram(bpmnModel, "png",
                            activityIds, Collections.emptyList(),
                            processEngineConfiguration.getActivityFontName(),
                            processEngineConfiguration.getLabelFontName(),
                            processEngineConfiguration.getAnnotationFontName(),
                            null, 1.0, false);
            out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int legth = 0;
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (null != in) {
                in.close();
            }
            if (null != out) {
                out.close();
            }
        }
        return out.toByteArray();
    }


    /**
     * 将javabean对象转换为map
     */
    private static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = new HashMap<>();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                Object value = beanMap.get(key);
                map.put(key + "", value);
            }
        }
        return map;
    }



}
