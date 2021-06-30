package com.kkedu.mvcframework.core;

public interface KKBeanFactory {
    Object getBean(Class beanClass);

    Object getBean(String beanName);
}
