package com.gyx.superscheduled.core.RunnableInterceptor.strengthen;

import com.gyx.superscheduled.core.RunnableInterceptor.SuperScheduledRunnable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;

public class ExecutionFlagStrengthen implements BaseStrengthen {
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * 前置强化方法
     *
     * @param bean   bean实例（或者是被代理的bean）
     * @param method 执行的方法对象
     * @param args   方法参数
     */
    @Override
    public void before(Object bean, Method method, Object[] args) {
        SuperScheduledRunnable superScheduledRunnable = (SuperScheduledRunnable) bean;
        logger.info("定时任务" + superScheduledRunnable.getSuperScheduledName() + "开始执行");
    }

    /**
     * 后置强化方法
     *
     * @param bean   bean实例（或者是被代理的bean）
     * @param method 执行的方法对象
     * @param args   方法参数
     */
    @Override
    public void after(Object bean, Method method, Object[] args) {
        SuperScheduledRunnable superScheduledRunnable = (SuperScheduledRunnable) bean;
        logger.info("定时任务" + superScheduledRunnable.getSuperScheduledName() + "开始执行");
    }

}
