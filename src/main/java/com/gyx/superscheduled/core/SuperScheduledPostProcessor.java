package com.gyx.superscheduled.core;

import com.gyx.superscheduled.exception.SuperScheduledException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Map;

@DependsOn("superScheduledConfig")
@Component
public class SuperScheduledPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    protected final Log logger = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean,
                                                  String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean,
                                                 String beanName) throws BeansException {
        SuperScheduledConfig superScheduledConfig = applicationContext.getBean(SuperScheduledConfig.class);

        //获取bean的方法
        Method[] methods = bean.getClass().getDeclaredMethods();
        if (methods.length > 0) {
            for (Method method : methods) {
                Scheduled annotation = method.getAnnotation(Scheduled.class);
                if (annotation == null) {
                    continue;
                }
                if (StringUtils.isEmpty(annotation.cron())) {
                    throw new SuperScheduledException("在" + beanName + "Bean中，存在缺少cron参数的SuperSchedule");
                }
                String name = beanName + "#" + method.getName();
                superScheduledConfig.addCronTrigger(name, new CronTrigger(annotation.cron()));
                try {
                    changeAnnotationValue(annotation, "cron", Scheduled.CRON_DISABLED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                superScheduledConfig.addRunnable(name, () -> {
                    try {
                        method.invoke(bean);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new SuperScheduledException(e.getLocalizedMessage());
                    }
                });
            }
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) throws Exception {
        InvocationHandler handler = Proxy.getInvocationHandler(annotation);
        Field f;
        try {
            f = handler.getClass().getDeclaredField("memberValues");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        f.setAccessible(true);
        Map<String, Object> memberValues;
        memberValues = (Map<String, Object>) f.get(handler);
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        memberValues.put(key, newValue);
        return oldValue;
    }
}