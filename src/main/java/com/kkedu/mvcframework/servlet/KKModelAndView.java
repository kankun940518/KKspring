package com.kkedu.mvcframework.servlet;

import com.sun.org.apache.regexp.internal.RE;

import java.util.Map;

public class KKModelAndView {

    private String viewName;
    private Map<String,?> model;

    public KKModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public KKModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String,?> getModel() {
        return model;
    }
}
