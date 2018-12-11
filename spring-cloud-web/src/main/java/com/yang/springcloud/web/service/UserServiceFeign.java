package com.yang.springcloud.web.service;

import com.yang.springcloud.web.service.impl.UserServiceFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "spring-cloud-provider",fallback = UserServiceFeignFallback.class)
public interface UserServiceFeign {

    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    String hello(@RequestParam(value = "id") String id);
}
