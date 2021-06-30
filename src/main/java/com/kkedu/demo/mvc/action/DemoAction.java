package com.kkedu.demo.mvc.action;


import com.kkedu.demo.service.IDemoService;
import com.kkedu.mvcframework.annotation.KKAutowired;
import com.kkedu.mvcframework.annotation.KKController;
import com.kkedu.mvcframework.annotation.KKRequestMapping;
import com.kkedu.mvcframework.annotation.KKRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@KKController
@KKRequestMapping("/demo")
public class DemoAction {

	@KKAutowired
	private IDemoService demoService;

	@KKRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @KKRequestParam("name") String name){
//		String result = demoService.get(name);
		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@KKRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@KKRequestParam("a") Integer a, @KKRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@KKRequestMapping("/remove")
	public void remove(HttpServletRequest req,HttpServletResponse resp,
					   @KKRequestParam("id") Integer id){
	}

}
