package com.kkedu.mvcframework.bean.config;

public class KKBeanDefinition {

    public boolean isLazyInit(){
        return false;
    }

    private String factoryBeanName;  //beanName

    private String beanClassName;   //原生类的全类名

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
