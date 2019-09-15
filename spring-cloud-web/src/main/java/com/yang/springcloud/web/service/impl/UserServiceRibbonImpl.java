package com.yang.springcloud.web.service.impl;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixRequestCache;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCollapser;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategyDefault;
import com.yang.springcloud.web.service.UserServiceRibbon;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
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
        return restTemplate.getForObject("http://spring-cloud-provider/hello?id={1}", String.class, id);
    }

    //如何获取异常信息呢，在helloFallback()方法中增加Throwable即可，如下
    private String helloFallback(String id, Throwable e) {
        return "hello...error.." + id;
    }

    /**
     * HystrixCommand：用在依赖返回单个操作结果的时候
     * HystrixObservableCommand：用来依赖的服务返回多个操作结果的时候
     *
     */

    /**
     * Command一共有4种命令的执行方式，在Hystrix会根据具体的实现类来实现，
     * HystrixObservableCommand实现了下面两个执行方式：
     * Observable<String> hotObservable=new UserCommand(restTemplate,1L).observe();
     * Observable<String> coldObservable=new UserCommand(restTemplate,1L).toObservable();
     * observe()：返回的是一个Hot Observable，该命令会在observe()调用的时候立即执行，当Observable每次被订阅的时候会重放它的行为。
     * toObservable()：返回的是一个Cold Observable，方法被调用后，命令不会立即执行，只有当所有订阅者都订阅之后才会执行，内部通过Semaphore类实现。
     * HystrixCommand实现了另外两种执行方式：
     * execute()：同步执行，从依赖的服务返回一个单一的结果对象，或是在发生错误的时候抛出异常。
     * queue()：异步执行，直接返回一个Future对象，其中包含了服务执行结束时要返回的单一结果对象。
     */

    //异步执行
//    @HystrixCommand
    public Future<String> getStringByIdAsync(final String id) {
        return new AsyncResult<String>() {
            @Override
            public String invoke() {
                return restTemplate.getForObject("http://spring-cloud-provider/hello?id={1}", String.class, id);
            }
        };
    }

    /**
     * 命令名称、分组以及线程池划分
     * 1、通过设置命令组，Hystrix会根据组来组织和统计命令的告警、仪表盘等信息。
     * 2、默认情况下，Hystrix会让相同组名的命令使用同一个线程池，所以我们需要在创建Hystrix命令时为其指定命令组名来实现默认的线程池划分。
     * 3、通过HystrixThreadPoolKey可以更细粒度对线程池进行设置
     * 4、通过注解的形式设置：
     * a、commandKey、groupKey、threadPoolKey分别表示了命令名称、分组以及线程池划分
     * b、@HystrixCommand(commandKey = "getUserById",groupKey = "UserGroup",threadPoolKey = "getUserByIdThread")
     * 请求缓存：
     * 1、通过重载getCacheKey()方法开启请求缓存
     * 2、通过HystrixRequestCache.clear()方法进行缓存的清理
     * 3、在AbstractCommand类中，如果子类不重写getCacheKey()方法，让它返回一个null，那么缓存功能是不会开启的，同时请求命令的缓存开启属性也需要设置为true才能开启，默认为true
     * 所以该属性是用来强制关闭请求缓存的功能的。
     * 4、注解方式实现请求缓存：@CacheResult、@CacheRemove、@CacheKey
     * a、@CacheResult：该注解用来标记请求命令返回的结果应该被缓存，它必须与@HystrixCommand注解结合使用
     * b、@CacheRemove：该注解用来让请求命令的缓存失效，失效的缓存根据定义的key决定
     * c、@CacheKey：该注解用来在请求命令的参数上标记，使其作为缓存的key值，如果没有标注则会使用所有参数。如果同时还是用了@CacheResult和@CacheRemove注解的cacheKeyMethod
     * 方法指定缓存的key的生成，那么该注解将不会起作用
     * 请求合并：
     * 1、实现：@HystrixCollapser(batchMethod = "findAll",collapserProperties = {@HystrixProperty(name = "timerDelayInMilliseconds",value = "100")})
     */
    private static class StringCommand extends com.netflix.hystrix.HystrixCommand<String> {

        private static final HystrixCommandKey GETTER_KEY = HystrixCommandKey.Factory.asKey("CommandKey");
        private RestTemplate restTemplate;
        private Long id;

        public StringCommand(RestTemplate restTemplate, Long id) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("StringGroup")).
                    andCommandKey(GETTER_KEY).
                    andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("ThreadPoolKey")));
            this.restTemplate = restTemplate;
            this.id = id;
        }

        @Override
        protected String run() throws Exception {
            return null;
        }

        //通过重载getCacheKey()方法开启请求缓存
        @Override
        protected String getCacheKey() {
            return String.valueOf(id);
        }

        //通过HystrixRequestCache.clear()方法进行缓存的清理
        public static void flushCache(Long id) {
            HystrixRequestCache.getInstance(GETTER_KEY, HystrixConcurrencyStrategyDefault.getInstance()).clear(String.valueOf(id));
        }

        //timerDelayInMilliseconds参数设置合并时间窗为100毫秒，在100毫秒的的所有请求都会被合并，一起发送
        @HystrixCollapser(batchMethod = "findAll", collapserProperties = {
                @HystrixProperty(name = "timerDelayInMilliseconds", value = "100")
        })
        public String findNameById(Long id) {
            return null;
        }

        @HystrixCommand
        public List findAll(List<Long> ids) {
            return restTemplate.getForObject("http://spring-cloud-provider/findAll?ids={}", List.class, StringUtils.join(ids, ","));
        }

        //属性配置，也可以单独为某一个方法定制配置
        @HystrixCommand(commandKey = "helloKey", commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "500")})
        public String findOrderById(Long id) {
            return null;
        }
    }
}
