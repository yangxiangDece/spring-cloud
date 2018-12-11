package com.yang.springcloud.web.service.impl;

import com.yang.springcloud.web.service.UserServiceRibbon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service(value = "userServiceRibbon")
public class UserServiceRibbonImpl implements UserServiceRibbon {

    @Autowired
    private RestTemplate restTemplate;

    @Override
//    @HystrixCommand(fallbackMethod = "helloFallback")
    public String hello(String id) {

//        return restTemplate.getForEntity("http://spring-cloud-provider/hello?id={1}",String.class,"12").getBody();
        return restTemplate.getForObject("http://spring-cloud-provider/hello?id={1}",String.class,id);
    }

    private String helloFallback(String id){
        return "hello...error.."+id;
    }
}
