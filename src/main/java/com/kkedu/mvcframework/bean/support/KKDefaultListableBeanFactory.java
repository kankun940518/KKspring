package com.kkedu.mvcframework.bean.support;

import com.kkedu.mvcframework.bean.config.KKBeanDefinition;
import com.kkedu.mvcframework.core.KKBeanFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KKDefaultListableBeanFactory implements KKBeanFactory {

    public Map<String,KKBeanDefinition> beanDefinitionMap = new HashMap<String,KKBeanDefinition>();

    @Override
    public Object getBean(Class beanClass) {
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        return null;
    }

    public void doRegistBeanDefinition(List<KKBeanDefinition> beanDefinitions) throws Exception{
        for (KKBeanDefinition beanDefinition : beanDefinitions) {
            if (this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The"+ beanDefinition.getFactoryBeanName()+"is exists!!!");
            }
            this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);

        }
    }
}
