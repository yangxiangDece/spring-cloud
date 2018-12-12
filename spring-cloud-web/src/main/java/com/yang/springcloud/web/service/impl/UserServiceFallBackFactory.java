package com.yang.springcloud.web.service.impl;

import com.yang.springcloud.web.service.UserServiceFeign;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserServiceFallBackFactory implements FallbackFactory<UserServiceFeign> {

    @Override
    public UserServiceFeign create(Throwable throwable) {
        return id -> "出错了，这是consumer客户端提供的服务降级信息，id:"+id;
    }
}
