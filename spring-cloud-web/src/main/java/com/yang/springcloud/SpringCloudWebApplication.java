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
 * 3、服务治理机制：
 *      a、服务提供者
 *          1、服务注册：服务提供者在启动的时候通过发送REST请求的方式将自己注册到Eureka Server上，同时带上自身服务的一些元数据信息。Eureka Server接收到这个REST
 *              请求之后，将元数据信息存储在一个双层Map中，其中第一层的key是服务名，第二层的key是具体服务的实例名。
 *              在服务注册时，需要确认eureka.client.fetch-register=true参数是否正确，默认为true。若设置成false将不会启动注册操作。
 *          2、服务同步：服务注册中心之间相互注册，当服务提供者发送注册请求到一个服务注册中心时，它会将该请求转发给集群中相连的其他注册中心，从而实现注册中心之间的服务同步。
 *              通过服务同步，两个服务提供者的服务信息可以通过这两台服务注册中心中任意一台获取到。
 *          3、服务续约：在注册完服务之后，服务提供者会维护一个心跳用来持续告诉Eureka Server我还活着，以防止Eureka Server的“剔除任务”将该服务从服务列表中排除出去。
 *              eureka.instance.lease-renewal-interval-in-seconds=30：参数用于定义服务续约任务的调用间隔时间，默认30秒。即：多久调用一次心跳
 *              eureka.instance.lease-expiration-duration-in-seconds=90：参数用于定义服务失效时间，默认90秒，即：如果Eureka Server在一定时间内（默认90秒）
 *              没有接收到某个微服务实例的心跳，Eureka Server将会移除该实例。
 *      b、服务消费者
 *          1、获取服务：当我们启动服务消费者的时候，它会发送一个REST请求给服务注册中心，来获取上面注册的服务清单。为了性能考虑，Eureka Server会维护一份
 *              只读的服务列表清单返回给客户端，同时该缓存清单会每隔30秒更新一次，可以通过下面这个参数修改更新缓存时间：
 *              eureka.client.register-fetch-interval-seconds=30，默认值30秒。
 *              注意：获取服务是服务消费者的基础，所以必须确保eureka.client.fetch-registry=true参数没有被更改为false，该值默认为true。
 *          2、服务调用：服务消费者在获取服务清单后，通过服务名可以获得具体提供服务的实例名和该实例的元数据信息。因为有这些服务实例的详细信息，所以客户端可以根据自己的需求
 *              决定具体调用哪个实例，在Ribbon中会默认采用轮询的方式进行调用，从而实现了客户端的负载均衡。
 *              Eureka中有Region和Zone的概念，一个Region中可以包含多个Zone，每个服务客户端需要被注册到一个Zone中，所以每个客户端对应一个Region和一个Zone。
 *              在进行服务调用的时候，优先访问同处一个Zone中的服务提供方，若访问不到，就访问其他的Zone。
 *          3、服务下线：在客户端程序中，当服务实例进行正常关闭操作时，它会触发一个服务下线的REST请求给Eureka Server，告诉服务注册中心，
 *              服务端在收到请求后，将该服务状态设置为下线(DOWN)，并把该下线事件广播出去。
 *     c、服务注册中心
 *          1、失效剔除：服务实例有时候并不一定会正常下线，可能由于内存溢出、网络故障等原因使得服务不能正常工作，而服务注册中心并未收到“服务下线”的请求。为了从服务列表中将这些
 *              无法提供服务的实例剔除，Eureka Server在启动的时候会创建一个定时任务，默认每隔一段时间（默认为60秒）将当前清单中超时（默认为90秒，
 *              可以通过eureka.instance.lease-expiration-duration-in-seconds=90参数设置）没有续约的服务剔除出去。
 *          2、自我保护：Eureka Server在运行期间，会统计心跳失败的比例在15分钟之内是否低于85%，如果出现低于的情况（在单机调试的时候很容易满足，因为我们经常重启服务，
 *              关闭服务等，实际在生产环境通常是由于网络不稳定导致），Eureka Server会将当前的实例注册信息保护起来，让这些实例不会过期，尽可能的保护这些实例，
 *              但是在保护这段时间，如果实例出现了问题，那么调用者拿到了已经不存在的实例，出现调用失败，所以客户端必须要有容错机制，比如请求重试、断路器等。
 *              在本地开发时，可以通过eureka.server.enable-self-preservation=false参数来关闭保护机制，确保注册中心可以将不可用的实例正确剔除。
 * 4、源码分析：
 *      a、
 *
 *
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
