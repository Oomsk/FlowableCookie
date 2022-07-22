package com.omsk.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @Author Sunjia'nan
 * @Date 2022/7/21 10:36
 * @Version 1.0
 */
public class SendRejectionMail implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        // 触发事件
        System.out.println("send email: " + "驳回申请");
    }
}
