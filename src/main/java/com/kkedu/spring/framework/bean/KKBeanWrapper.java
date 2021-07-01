package com.kkedu.spring.framework.bean;

public class KKBeanWrapper {

    private Object wrappedInstance;

    private Class<?> wrappedClass;


    public KKBeanWrapper(Object instance) {
        this.wrappedInstance = instance;
        this.wrappedClass = instance.getClass();

    }

    public Object getWrappedInstance(){
        return this.wrappedInstance;
    }

    public Class<?> getWrappedClass(){
        return this.wrappedClass;
    }
}
