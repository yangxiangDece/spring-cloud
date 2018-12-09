package com.yang.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 1、Ribbon是一个基于HTTP和TCP的客户端负载均衡器，它可以在通过客户端配置的ribbonServerList服务端列表去轮询访问达到负载均衡的作用。当Ribbon与Eureka联合使用时，
 *      Ribbon的服务实例清单RibbonServerList会被DiscoveryEnableNIWSServerList重写，扩展成Eureka注册中心中获取服务端列表。同时它会用NIEWSDiscoveryPing来取代
 *      IPing，它将职责委托给Eureka来确定服务端是否已经启动。
 * 2、如果注册中心是Eureka，推荐使用注解：@EnableEurekaClient；如果注册中心是其他的，则使用：@EnableDiscoveryClient
 *
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpringCloudWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWebApplication.class, args);
	}

	@Bean
    @LoadBalanced
	public RestTemplate restTemplate(){
	    return new RestTemplate();
    }
}
