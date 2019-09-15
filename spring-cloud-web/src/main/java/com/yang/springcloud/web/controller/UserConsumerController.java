package com.yang.springcloud.web.controller;

import com.yang.springcloud.web.service.UserServiceRibbon;
import com.yang.springcloud.web.service.UserServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserConsumerController {

    @Autowired
    private UserServiceRibbon userServiceRibbon;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @GetMapping(value = "/consumer")
    public String helloConsumer(){
        return userServiceRibbon.hello("12");
    }

    @GetMapping(value = "/consumer-feign")
    public String helloConsumerFeign(){
        return userServiceFeign.hello("12");
    }

}
