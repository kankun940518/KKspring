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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KKDispatcherServlet extends HttpServlet {

    //保存url和Method的对应关系
    private List<KKHandlerMapping> handlerMappings = new ArrayList<KKHandlerMapping>();

    private Map<KKHandlerMapping,KKHandlerAdapter> handlerAdapters = new HashMap<KKHandlerMapping,KKHandlerAdapter>();

    private List<KKViewResolver> viewResolvers = new ArrayList<KKViewResolver>();
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
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("detail","500 Excpetion Detail:");
            model.put("stackTrace",Arrays.toString(e.getStackTrace()));
            try {
                processDispatchResult(req,resp,new KKModelAndView("500",model));
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1、根据Url 拿到对应的Handler
        KKHandlerMapping handler = getHandler(req);

        if (null == handler){
            processDispatchResult(req,resp,new KKModelAndView("404"));
            return;
        }

        //2、根据HandlerMapping 去拿到Adapter
        KKHandlerAdapter ha = getHandlerAdapter(handler);

        //3、根据HandlerAdapter拿到对应的ModerAndView
        KKModelAndView mv = ha.handle(req,resp,handler);

        //4、根据ViewResolver找到对应的View对象
        //通过View对象渲染页面，返回
        processDispatchResult(req,resp,mv);
       /* String url = req.getRequestURI();
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
        method.invoke(applicationContext.getBean(beanName),paramValues);*/
    }



    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, KKModelAndView mv) throws Exception {
        if (null == mv){return; }
        if (this.viewResolvers.isEmpty()){return;}

        for (KKViewResolver viewResolver : this.viewResolvers) {
            KKView view = viewResolver.resolverViewName(mv.getViewName());
            view.render(mv.getModel(),req,resp);
            return;
        }
    }

    private KKHandlerAdapter getHandlerAdapter(KKHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()){return null;}
        KKHandlerAdapter ha = this.handlerAdapters.get(handler);
        return ha;
    }

    private KKHandlerMapping getHandler(HttpServletRequest req) {
       String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        for (KKHandlerMapping handlerMapping : this.handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if (!matcher.matches()){continue;}
            return handlerMapping;
        }
        return null;

    }

    @Override
    public void init(ServletConfig config) throws ServletException {



        applicationContext = new KKApplicationContext(config.getInitParameter("contextConfigLocation"));
        //========MVC=========
        initStrategies(applicationContext);

        //5、初始化HandlerMapping
       /* doInitHandlerMapping();*/
        System.out.println("KK Spring framework is init!");
    }
    //初始化策略
    private void initStrategies(KKApplicationContext context) {
        //handlerMapping
        initHandlerMappings(context);

        //初始化参数适配器
        initHandlerAdapters(context);
        //初始化视图转换器
        initViewResolvers(context);

    }

    private void initViewResolvers(KKApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateDir =new File(templateRootPath);
        for (File file : templateDir.listFiles()) {
            this.viewResolvers.add(new KKViewResolver(templateRoot));
        }
    }

    private void initHandlerAdapters(KKApplicationContext context) {
        for (KKHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping,new KKHandlerAdapter());
        }
    }

    private void initHandlerMappings(KKApplicationContext context) {

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

                String regex = ("/"+baseUrl+"/"+requestMapping.value())
                        .replaceAll("\\*",".*")
                        .replaceAll("/+","/");
                Pattern pattern =Pattern.compile(regex);
                handlerMappings.add(new KKHandlerMapping(pattern,instance,method));
                System.out.println("Mapped:"+regex+"------>"+method);
            }
        }
        
    }

    //初始化url和Method的一对一对应关系
  /*  private void doInitHandlerMapping() {
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
                handlerMappings.put(url,method);
                System.out.println("Mapped:"+url+","+method);
            }
        }
    }*/

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
