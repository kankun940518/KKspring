package com.kkedu.demo.action;


import com.kkedu.demo.service.IModifyService;
import com.kkedu.demo.service.IQueryService;
import com.kkedu.mvcframework.annotation.KKAutowired;
import com.kkedu.mvcframework.annotation.KKController;
import com.kkedu.mvcframework.annotation.KKRequestMapping;
import com.kkedu.mvcframework.annotation.KKRequestParam;
import com.kkedu.mvcframework.servlet.KKModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 公布接口url
 * @author Tom
 *
 */
@KKController
@KKRequestMapping("/web")
public class MyAction {

	@KKAutowired
	IQueryService queryService;
	@KKAutowired
	IModifyService modifyService;

	@KKRequestMapping("/query.json")
	public KKModelAndView query(HttpServletRequest request, HttpServletResponse response,
								@KKRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
	@KKRequestMapping("/add*.json")
	public KKModelAndView add(HttpServletRequest request,HttpServletResponse response,
			   @KKRequestParam("name") String name,@KKRequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		return out(response,result);
	}
	
	@KKRequestMapping("/remove.json")
	public KKModelAndView remove(HttpServletRequest request, HttpServletResponse response,
								 @KKRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@KKRequestMapping("/edit.json")
	public KKModelAndView edit(HttpServletRequest request,HttpServletResponse response,
			@KKRequestParam("id") Integer id,
			@KKRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private KKModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
