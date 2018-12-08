package com.yang.springcloud.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 如果注册中心是Eureka，推荐使用注解：@EnableEurekaClient
 * 如果注册中心是其他的，则使用：@EnableDiscoveryClient
 *
 */
@SpringBootApplication
@EnableEurekaClient
public class SpringCloudProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudProviderApplication.class, args);
	}
}
