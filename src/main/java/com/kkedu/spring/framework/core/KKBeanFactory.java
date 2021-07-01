package com.kkedu.spring.framework.core;

public interface KKBeanFactory {
    Object getBean(Class beanClass);

    Object getBean(String beanName);
}
