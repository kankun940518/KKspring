package com.kkedu.demo.action;

import com.kkedu.spring.framework.annotation.KKAutowired;
import com.kkedu.spring.framework.annotation.KKController;
import com.kkedu.spring.framework.annotation.KKRequestMapping;
import com.kkedu.spring.framework.annotation.KKRequestParam;
import com.kkedu.demo.service.IQueryService;
import com.kkedu.spring.framework.servlet.KKModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@KKController
@KKRequestMapping("/")
public class PageAction {

    @KKAutowired
    IQueryService queryService;

    @KKRequestMapping("/first.html")
    public KKModelAndView query(@KKRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new KKModelAndView("first.html",model);
    }

}
