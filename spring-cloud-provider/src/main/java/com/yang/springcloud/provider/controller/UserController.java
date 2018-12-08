package com.yang.springcloud.provider.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final static Logger LOGGER= LoggerFactory.getLogger(UserController.class);


    @GetMapping(value = "/hello")
    public String hello(){
        LOGGER.info("/hello,host:{},service_id:{}");
        return "Hello World";
    }
}
