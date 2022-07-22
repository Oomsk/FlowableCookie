package com.omsk.flowable.listener;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

/**
 * @Author Sunjia'nan
 * @Date 2022/7/21 10:27
 * @Version 1.0
 */

public class approveTaskHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        delegateTask.setAssignee("lisi");
    }
}
