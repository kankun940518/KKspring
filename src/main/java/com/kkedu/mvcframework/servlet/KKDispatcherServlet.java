package com.kkedu.mvcframework.servlet;

import com.kkedu.mvcframework.annotation.*;
import com.kkedu.mvcframework.context.KKApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class KKDispatcherServlet extends HttpServlet {

    //保存url和Method的对应关系
    private Map<String,Method> handlerMapping = new HashMap<String,Method>();

    //ioc容器
    private KKApplicationContext applicationContext = null;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6、根据url调用method
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception Detail:"+Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!!");
            return;
        }

        Method method = this.handlerMapping.get(url);

        //1、先把形参的位置和参数名字建立映射关系，并且缓存下来
        Map<String,Integer> paramIndexMapping = new HashMap<String, Integer>();

        Annotation [][] pa = method.getParameterAnnotations();
        for (int i = 0; i < pa.length; i ++) {
            for (Annotation a : pa[i]) {
                if(a instanceof KKRequestParam){
                    String paramName = ((KKRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }

        Class<?> [] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];
            if(type == HttpServletRequest.class || type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }

        //2、根据参数位置匹配参数名字，从url中取到参数名字对应的值
        Object[] paramValues = new Object[paramTypes.length];

        //http://localhost/demo/query?name=Tom&name=Tomcat&name=Mic
        Map<String,String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue())
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s","");

            if(!paramIndexMapping.containsKey(param.getKey())){continue;}

            int index = paramIndexMapping.get(param.getKey());

            //涉及到类型强制转换
            paramValues[index] = value;
        }

        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        //3、组成动态实际参数列表，传给反射调用
        method.invoke(applicationContext.getBean(beanName),paramValues);
    }
    @Override
    public void init(ServletConfig config) throws ServletException {



        applicationContext = new KKApplicationContext(config.getInitParameter("contextConfigLocation"));


        //5、初始化HandlerMapping
        doInitHandlerMapping();
        System.out.println("KK Spring framework is init!");
    }

    //初始化url和Method的一对一对应关系
    private void doInitHandlerMapping() {
        if (this.applicationContext.getBeanDefinitionCount() == 0){ return;}
        for (String beanName :this.applicationContext.getBeanDefinitionNames()) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();
            if (!clazz.isAnnotationPresent(KKController.class)){continue;}
            //保存写在类上面的@KKRequestMapping("/demo")
            String baseUrl = "";
            if (clazz.isAnnotationPresent(KKRequestMapping.class)){
                KKRequestMapping requestMapping = clazz.getAnnotation(KKRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            //默认获取所有的public方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(KKRequestMapping.class)){continue;}
                KKRequestMapping requestMapping = method.getAnnotation(KKRequestMapping.class);
                //避免出现 demoquery或者demo//query的情况
                String url = ("/"+baseUrl+"/"+requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println("Mapped:"+url+","+method);
            }
        }
    }

    //如果类名本身是小写字母，确实会出问题
    //但是我要说明的是：这个方法是我自己用，private的
    //传值也是自己传，类也都遵循了驼峰命名法
    //默认传入的值，存在首字母小写的情况，也不可能出现非字母的情况
    //为了简化程序逻辑，就不做其他判断了，大家了解就OK
    //其实用写注释的时间都能够把逻辑写完
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        //之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
