package com.kkedu.mvcframework.context;

import com.kkedu.mvcframework.annotation.KKAutowired;
import com.kkedu.mvcframework.annotation.KKController;
import com.kkedu.mvcframework.annotation.KKService;
import com.kkedu.mvcframework.bean.KKBeanWrapper;
import com.kkedu.mvcframework.bean.config.KKBeanDefinition;
import com.kkedu.mvcframework.bean.support.KKBeanDefinitionReader;
import com.kkedu.mvcframework.bean.support.KKDefaultListableBeanFactory;
import com.kkedu.mvcframework.core.KKBeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KKApplicationContext implements KKBeanFactory {

    private KKDefaultListableBeanFactory registry = new KKDefaultListableBeanFactory();

    //三级缓存 ,终极缓存
    private Map<String,KKBeanWrapper> factoryBeanInstanceCache = new HashMap<String,KKBeanWrapper>();

    private Map<String,Object> factoryBeanObjectCache = new HashMap<String,Object>();

    private KKBeanDefinitionReader reader;

    public KKApplicationContext(String ...configLocations) {
        //1、加载配置文件
        reader = new KKBeanDefinitionReader(configLocations);
        try {

            //2、解析配置文件,讲所有的配置信息封装成BeanDefinition对象
            List<KKBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

            //3、所有的配置信息缓存起来
            this.registry.doRegistBeanDefinition(beanDefinitions);

            //4、加载非延时加载的所有的bean
            doLoadInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doLoadInstance() {
        //循环调用getBean()方法
        for (Map.Entry<String, KKBeanDefinition> entry : this.registry.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            if (!entry.getValue().isLazyInit()){
                getBean(beanName);
            }
        }

    }

    @Override
    public Object getBean(Class beanClass) {
        return getBean(beanClass.getName());
    }


    @Override
    public Object getBean(String beanName) {
        //1、先拿到BeanDefinition配置信息
        KKBeanDefinition beanDefinition = registry.beanDefinitionMap.get(beanName);

        //2、反射实例化对象
        Object instance = instantiateBean(beanName,beanDefinition);

        //3、将返回的Bean对象封装成BeanWrapper
        KKBeanWrapper beanWrapper = new KKBeanWrapper(instance);

        //4、执行依赖注入
        populateBean(beanName,beanDefinition,beanWrapper);

        //5、保存到IoC容器中
        this.factoryBeanInstanceCache.put(beanName,beanWrapper);

        return beanWrapper.getWrappedInstance();
    }

    private void populateBean(String beanName, KKBeanDefinition beanDefinition, KKBeanWrapper beanWrapper) {

        Object instance = beanWrapper.getWrappedInstance();

        Class<?> clazz = beanWrapper.getWrappedClass();

        if (!(clazz.isAnnotationPresent(KKController.class)||clazz.isAnnotationPresent(KKService.class))){return;}

        //拿到实例的所有的字段
        //Declared 所有的，特定的 字段，包括private/protected/default
        //正常来说，普通的OOP编程只能拿到public的属性
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(KKAutowired.class)){
                continue;
            }
            KKAutowired autowired = field.getAnnotation(KKAutowired.class);
            //如果用户没有自定义beanName，默认就根据类型注入
            //这个地方省去了对类名首字母小写的情况的判断，这个作为课后作业
            //小伙伴们自己去完善
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)){
                //获得接口的类型，作为key待会拿这个key到ioc容器中去取值
                autowiredBeanName = field.getType().getName();
            }
            //如果是public以外的修饰符，只要加了@Autowired注解，都要强制赋值
            //反射中叫做暴力访问
            field.setAccessible(true);
            //反射调用的方式
            //给entry.getValue()这个对象的field字段，赋ioc.get(beanName)这个值
            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName)==null){
                    continue;
                }
                //依赖注入,实际上这里就是自动赋值
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Object instantiateBean(String beanName, KKBeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();

            //如果是代理对象  触发AOP的逻辑  后面AOP的入口

            this.factoryBeanObjectCache.put(beanName,instance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }

    public int getBeanDefinitionCount(){
        return this.registry.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames(){
        return this.registry.beanDefinitionMap.keySet().toArray(new String[0]);
    }
}