package com.yang.springcloud.web.service.impl;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.yang.springcloud.web.service.UserServiceRibbon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Future;

@Service(value = "userServiceRibbon")
public class UserServiceRibbonImpl implements UserServiceRibbon {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * ignoreExceptions = {HystrixBadRequestException.class}：忽略指定异常，当出现这些异常是，不触发熔断机制
     * 在HystrixCommand实现的run()方法中抛出异常时，除了HystrixBadRequestException之外，其他的异常均会被Hystrix认为命令执行失败并触发服务降级的处理，所以
     * 当需要在命令执行的时候抛出不触发服务降级的异常，就需要使用ignoreExceptions。
     * 原理：当方法抛出异常是，Hystrix会将它包装在HystrixBadRequestException中抛出，这样就不会触发后续的fallbackMethod方法。
     */
    @Override
//    @HystrixCommand(fallbackMethod = "helloFallback")
//    @HystrixCommand(fallbackMethod = "helloFallback",ignoreExceptions = {HystrixBadRequestException.class})
    public String hello(String id) {

//        return restTemplate.getForEntity("http://spring-cloud-provider/hello?id={1}",String.class,"12").getBody();
        return restTemplate.getForObject("http://spring-cloud-provider/hello?id={1}",String.class,id);
    }

    //如何获取异常信息呢，在helloFallback()方法中增加Throwable即可，如下
    private String helloFallback(String id,Throwable e){
        return "hello...error.."+id;
    }

    /**
     * HystrixCommand：用在依赖返回单个操作结果的时候
     * HystrixObservableCommand：用来依赖的服务返回多个操作结果的时候
     *
     */

    /**
     * Command一共有4种命令的执行方式，在Hystrix会根据具体的实现类来实现，
     * HystrixObservableCommand实现了下面两个执行方式：
     *      Observable<String> hotObservable=new UserCommand(restTemplate,1L).observe();
     *      Observable<String> coldObservable=new UserCommand(restTemplate,1L).toObservable();
     *          observe()：返回的是一个Hot Observable，该命令会在observe()调用的时候立即执行，当Observable每次被订阅的时候会重放它的行为。
     *          toObservable()：返回的是一个Cold Observable，方法被调用后，命令不会立即执行，只有当所有订阅者都订阅之后才会执行，内部通过Semaphore类实现。
     * HystrixCommand实现了另外两种执行方式：
     *      execute()：同步执行，从依赖的服务返回一个单一的结果对象，或是在发生错误的时候抛出异常。
     *      queue()：异步执行，直接返回一个Future对象，其中包含了服务执行结束时要返回的单一结果对象。
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
