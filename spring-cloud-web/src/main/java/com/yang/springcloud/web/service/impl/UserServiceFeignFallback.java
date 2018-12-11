package com.yang.springcloud.web.service.impl;

import com.yang.springcloud.web.service.UserServiceFeign;
import org.springframework.stereotype.Component;

@Component
public class UserServiceFeignFallback implements UserServiceFeign {

    @Override
    public String hello(String id) {
        return "fall back error...."+id;
    }
}
