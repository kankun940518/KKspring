package com.kkedu.demo.service.impl;

import com.kkedu.demo.service.IDemoService;
import com.kkedu.mvcframework.annotation.KKService;

@KKService
public class DemoService implements IDemoService {
    @Override
    public String get(String name) {
        return "My name is " + name;
    }
}
