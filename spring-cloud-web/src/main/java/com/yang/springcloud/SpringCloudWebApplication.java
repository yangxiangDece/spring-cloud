package com.yang.springcloud;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

//@SpringCloudApplication //这个注解 涵盖了下面这三个注解，可以直接只是用下面这一个，说明一个Spring Cloud标准应用应该包含服务发现以及断路器
@SpringBootApplication
@EnableDiscoveryClient //启用注册服务与发现服务
//@EnableCircuitBreaker  //开启熔断机制
@EnableFeignClients //Feign功能包含了Ribbon与Hystrix
//这个自定义配置类不能放在@ComponentScan所扫描的包以及子包下面，否则这个自定义配置类会被所有的Ribbon客户端所共享，也就是定义成了全局的了，就达不到特殊化定制的目的了。
//@RibbonClient(value = "spring-cloud-provider",configuration = MySelfRule.class) //对于调用spring-cloud-provider服务，使用我们自己的负载均衡策略
public class SpringCloudWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudWebApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    //修改默认负载均衡算法，会覆盖Spring Cloud Ribbon默认的负载均衡算法，这个是全局的Ribbon负载均衡策略配置。需要自定义看上面的@RibbonClient
    @Bean
    public IRule randomRule() {
        return new RandomRule();//随机算法
        //它通过遍历负债均衡中维护的所有服务实例，并找出并发请求最小（内部维护了一个统计调用信息）的一个，所以该策略的特性是可选出最大空闲的实例。
        //同时，由于该算法的核心依据是统计对象loadBalancerStats，当其为空的时候，该策略是无法执行的。在源码中，当loadBalancerStats为空时，它会采用父类的线性轮询策略。
//	    return new BestAvailableRule();
        //先按照RoundRobinRule的策略获取服务，如果获取服务失败以后，在指定的时间内会进行几次重试，获取可用的服务，
        //比如：三台消费者，第二台挂了，轮询几次发现第二台都是访问不了的，那么后面就会将第二台抛弃，只会轮询第一台和第三台了
//        return new RetryRule();
    }
}
