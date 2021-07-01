package com.kkedu.spring.framework.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class KKHandlerMapping {

    private Object controller;
    protected Method method;
    protected Pattern pattern;

    public KKHandlerMapping(Pattern pattern,Object controller, Method method) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
