package com.yang.springcloud.web.service.impl;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import com.yang.springcloud.web.service.UserServiceRibbon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Future;

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

    /**
     * Observable<String> hotObservable=new UserCommand(restTemplate,1L).observe();
     * Observable<String> coldObservable=new UserCommand(restTemplate,1L).toObservable();
     * observe()：返回的是一个Hot Observable，该命令会在observe()
     */

    //异步执行
    @HystrixCommand
    public Future<String> getStringByIdAsync(final String id){
        return new AsyncResult<String>() {
            @Override
            public String invoke() {
                return restTemplate.getForObject("http://spring-cloud-provider/hello?id={1}",String.class,id);
            }
        };
    }
}
