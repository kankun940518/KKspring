package com.kkedu.spring.framework.servlet;

import java.io.File;

public class KKViewResolver {

    //.vm   .ftl  .jsp  .gp  .tom
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateDir;

    public KKViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateDir = new File(templateRootPath);
    }

    public KKView resolverViewName(String viewName){
        if (null == viewName ||"".equals(viewName.trim())){return null;}
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)?viewName :(viewName+DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((templateDir.getPath()+"/"+viewName)
                .replaceAll("/+","/"));
        return new KKView(templateFile);

    }
}
