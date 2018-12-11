package com.yang.springcloud;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 1、Ribbon是一个基于HTTP和TCP的客户端负载均衡器，它可以在通过客户端配置的ribbonServerList服务端列表去轮询访问达到负载均衡的作用。当Ribbon与Eureka联合使用时，
 *      Ribbon的服务实例清单RibbonServerList会被DiscoveryEnableNIWSServerList重写，扩展成Eureka注册中心中获取服务端列表。同时它会用NIEWSDiscoveryPing来取代
 *      IPing，它将职责委托给Eureka来确定服务端是否已经启动。
 * 2、如果注册中心是Eureka，推荐使用注解：@EnableEurekaClient；如果注册中心是其他的，则使用：@EnableDiscoveryClient
 * 3、Spring Cloud Eureka：
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
 * 4、eureka源码分析：
 *      a、Region、Zone：一个服务只有一个Region，一个Region可以有多个Zone。当我们使用Ribbon来实现服务调用时，对于Zone的设置可以在负载均衡时实现区域亲和特性：Ribbon的默认策略
 *          会优先访问同客户端处于一个Zone中的服务端实例，只有当同一个Zone中没有可用服务端实例的时候才访问其他Zone中的实例。所以通过Zone属性的定义，配合实际部署的物理结构，我们
 *          就可以有效地设计出对区域特性故障的容错集群。
 *      b、服务注册：通过EndpointUtils.getServiceUrlsFromConfig(EurekaClientConfig clientConfig, String instanceZone, boolean preferSameZone)解析Region、Zone等信息，
 *          如果没有配置Region、Zone，那么就是用默认的defaultZone，这就是我们自己配置的...serverUrls.defaultZone.
 *          默认serverUrls.defaultZone：public static final String DEFAULT_URL = "http://localhost:8761" + DEFAULT_PREFIX + "/";
 *          最终会调用DiscoveryClient类构造方法 -> initScheduledTasks() -> InstanceInfoReplicator类的run()方法 -> discoveryClient.register() ->
 *          httpResponse = eurekaTransport.registrationClient.register(instanceInfo); 会把配置的这些信息封装成一个instanceInfo对象，通过REST请求发送给Eureka服务器
 *      c、服务续约：服务续约和服务注册在同一个方法initScheduledTasks(),同一个if判断中if (clientConfig.shouldRegisterWithEureka()) {
 *              int renewalIntervalInSecs = instanceInfo.getLeaseInfo().getRenewalIntervalInSecs();参数用于定义服务续约任务的调用间隔时间，默认30秒。即：多久调用一次心跳
 *              int expBackOffBound = clientConfig.getHeartbeatExecutorExponentialBackOffBound();参数用于定义服务失效时间，默认90秒，即：如果Eureka Server在一定时间内（默认90秒）
 *          }
 *      d、服务获取：initScheduledTasks() -> if (clientConfig.shouldFetchRegistry()) {
 *              //服务列表清单缓存更新间隔时间
 *              int registryFetchIntervalSeconds = clientConfig.getRegistryFetchIntervalSeconds();即：eureka.client.register-fetch-interval-seconds=30，默认值30秒。
 *          }
 *      e、eureka服务注册中心：InstanceRegistry类的register(final InstanceInfo info, final boolean isReplication) ->
 *          1、handleRegistration();将新服务注册的事件传播出去，然后调用父类的注册方法
 *          2、super.register(info, isReplication); 注册中心存储了两层Map结构，第一层key存储服务名(即：eureka.instance.appName或spring.application.name)，第二层key存储实例名：
 *              InstanceInfo中的instanceId属性，存储MAP结构如下：
 *              ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry= new ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>();
 *      f、配置信息：
 *          1、服务注册中心配置：org.springframework.cloud.netflix.eureka.server.EurekaServerConfigBean，配置都是以eureka.server开头
 *          2、服务注册类配置：org.springframework.cloud.netflix.eureka.EurekaClientConfigBean，配置都是以eureka.client开头
 *          3、服务实例类配置：org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean，配置都是以eureka.instance开头
 *      g、端点配置：homePageUrl、statusPageUrl、healthCheckUrl，它们分别代表了应用主页的URL、状态页的URL、健康检查的URL。其中，状态页和健康检查的URL在Spring Cloud Eureka中默认
 *          使用了spring-boot-actuator模块提供的/info断点和/health端点。为了服务的正常运作，我们必须确保Eureka客户端的/health端点在发送元数据的时候，是一个能够被注册访问到的地址，
 *          否则服务注册中心不会根据应用的健康检查来更改状态。
 *          在大多数情况下我们不需更改这些配置，但是有些情况下需要更改，比如，为应用添加了context-path，前缀发生变化，就需要更改配置
 *              management.server.servlet.context-path=/hello
 *              eureka.instance.statusPageUrlPath=${management.server.servlet.context-path}/info
 *              eureka.instance.healthCheckUrlPath=${management.server.servlet.context-path}/health
 *          上面的配置都是使用的相对路径来配置的，当客户端以HTTPS的方式来暴露服务和监控端点时，需要更改配置
 *              eureka.instance.statusPageUrlPath=${eureka.instance.hostname}/info
 *              eureka.instance.healthCheckUrl=${eureka.instance.hostname}/health
 *              eureka.instance.homePageUrl=${eureka.instance.hostname}/
 * 5、Spring Cloud Ribbon
 *      a、配置类：org.springframework.cloud.netflix.ribbon.eureka.RibbonEurekaAutoConfiguration，自动配置类
 *      b、客户端负载均衡器的自动化配置类：org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration
 *      c、在引入Spring Cloud Ribbon的依赖之后，就能够自动化构建下面这些接口的实现：
 *          1、IClientConfig：Ribbon的客户端配置，默认实现：com.netflix.client.config.DefaultClientConfigImpl
 *          2、IRule：Ribbon的负载均衡策略，默认实现：com.netflix.loadbalancer.ZoneAvoidanceRule。该策略能够在多区域下选出最佳的实例进行访问。
 *          3、IPing：Ribbon的实例检查策略，默认实现：com.netflix.loadbalancer.NoOpPing。该检查策略是一个特殊的实现，实际上它并不会检查实例是否可用，而是始终返回true，
 *              默认认为所有服务实例都是可用的。
 *          4、ServerList<Server>：服务实例清单的维护机制，默认实现：com.netflix.loadbalancer.ConfigurationBasedServerList。
 *          5、ServerListFilter<Server>：服务实例清单过滤机制，默认实现：org.springframework.cloud.netflix.ribbon.ZonePreferenceServerListFilter。
 *              该策略能够优先过滤出与请求调用放处于同区域的服务实例。
 *          6、ILoadBalancer：负载均衡器，默认实现：com.netflix.loadbalancer.ZoneAwareLoadBalancer。它具备了区域感知的能力。
 *          这些自动化配置内容仅在没有引入Spring Cloud Eureka等服务治理框架时如此，在同时引入了Eureka和Ribbon依赖时，自动化配置会有一些不同。
 *          针对一些个性化需求，我们也可以自定义自己的默认实现，只需在Spring Boot应用中创建对应的实现实例就能覆盖这些默认的配置实现。
 *              比如下面，由于创建了PingUrl,所以默认的NoOpPing就不会被创建：
 *              @Bean
 *              public IPing ribbonPing(IClientConfig config){
 *                  return new PingUrl();
 *              }
 *          另外，也可以通过@RibbonClient注解来实现更细粒度的客户端配置。
 *      e、在Camden版本中可以使用配置的方式来实现：
 *          1、在application.properties配置中增加：
 *              hello-service.ribbon.NFLoadBalancerPingClassName=com.netfilx.loadbalancer.PingUrl  这个配置和上面的实现效果一样
 *              其中hello-service为服务名，NFLoadBalancerPingClassName参数用来指定具体的IPing接口实现类，在Camden版本中，Spring Cloud Ribbon新增了一个
 *              org.springframework.cloud.netflix.ribbon.PropertiesFactory类，可以动态地为RibbonClient创建这些接口的实现：
                    public PropertiesFactory() {
                        classToProperty.put(ILoadBalancer.class, "NFLoadBalancerClassName");
                        classToProperty.put(IPing.class, "NFLoadBalancerPingClassName");
                        classToProperty.put(IRule.class, "NFLoadBalancerRuleClassName");
                        classToProperty.put(ServerList.class, "NIWSServerListClassName");
                        classToProperty.put(ServerListFilter.class, "NIWSServerListFilterClassName");
                    }
 *      d、Ribbon与Eureka结合：
 *          1、当Spring Cloud的应用中同时加入Eureka和Ribbon依赖时，会触发Eureka中实现对Ribbon的自动化配置。
 *          2、ServerList的维护机制实现将被覆盖，为：com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList，该实现会将服务清单交给Eureka的服务治理机制来进行维护。
 *          3、IPing的实现将被覆盖，为：com.netflix.niws.loadbalancer.NIWSDiscoveryPing，该实现也将实例检查的任务交给了服务治理框架来进行维护。
 *          4、默认情况下，用于获取实例请求的ServerList接口实现将采用Spring Cloud Eureka中封装的org.springframework.cloud.netflix.ribbon.eureka.DomainExtractingServerList，
 *              其目的是为了让实例维护策略更加通用，所以将物理元数据来进行负载均衡，而不是使用原生的AWS AMI元数据。
 *          5、在Eureka结合下的配置方式：eureka.instance.metadataMap.zone=shanghai
 *          6、在Eureka和Ribbon结合的工程中，也可以通过参数配置来禁用Eureka堆Ribbon服务实例的维护实现：ribbon.eureka.enable=true，
 *              那么就需要我们按照之前的配置方式配置了，<client>.ribbon.listOfServers=...
 *      f、重试机制：
 *          1、Spring Cloud Eureka实现的服务治理机制强调了CAP原理中的AP，即可用性与可靠性，Eureka为了实现更高的服务可用性，牺牲了一定的一致性。
 *          2、Spring Cloud整合了Spring Retry来增强RestTemplate的重试能力，配置如下：
 *              spring.cloud.loadbalancer.retry.enabled=true; 开启重试价值，默认是关闭的。可查看配置类：LoadBalancerRetryProperties
 * 6、Spring Cloud Hystrix
 *      a、@SpringBootApplication注解涵盖了：@SpringBootApplication、@EnableDiscoveryClient、@EnableCircuitBreaker，说明一个Spring Cloud标准应用应该包含服务发现以及断路器
 * 7、Spring Cloud Feign
 *      a、Feign功能包含了Ribbon与Hystrix
 *      b、在@FeignClient注解标注的接口中定义的方法，@RequestParam、@RequestHeader等可以指定参数的名称，它们的value不能少，否则会抛出IllegalStateException异常，value属性不能为空。
 *      c、继承特性：在@FeignClient注解标注的接口中，这里的方法服务提供者的Controller一致，为了避免重复的代码，我们可以在公共api中定义接口以及实现类，然后消费者的接口中只需要，创建一个
 *          接口，然后标注@FeignClient注解，继承公共api中的接口即可。
 *      d、由于Spring Cloud Feign的客户端负载均衡是通过Spring Cloud Ribbon实现的，所以我们可以直接配置Ribbon客户端的方式来自定义各个服务客户端调用的参数。
 *          spring-cloud-provider.ribbon.ConnectionTimeout=500
 *          spring-cloud-provider.ribbon.readTimeout=2000
 *          spring-cloud-provider.ribbon.MaxAutoRetries=2
 *      e、Ribbon的超时与Hystrix的超时是两个概念，一般是需要让Hystrix的超时时间大于Ribbon的超时时间，否则Hystrix命令超时后，该命令直接熔断，重试机制就没有任何意义了。
 *      f、对于Hystrix的全局配置同Spring Cloud Ribbon的全局配置一致，直接使用它的默认配置前缀hystrix.command.default就可以设置，比如设置全局的超时时间：
 *          hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=5000
 *          另外，在对Hystrix进行配置之前，我们需要确认feign.hystrix.enabled参数没有被设置为false，否则该参数设置会关闭Feign客户端的Hystrix支持。
 *          可以通过feign.hystrix.enabled=false来关闭Hystrix功能，或者使用hystrix.command.default.execution.timeout.enabled=false来关闭熔断功能
 *      g、请求压缩：Spring Cloud Feign支持对请求与响应进行GZIP压缩，以减少通信过程中的性能损耗。开启请求与响应的压缩功能：
 *          feign.compression.request.enabled=true
 *          feign.compression.response.enabled=true
 *          可以设置请求压缩的大小下限制，只有超过这个大小的请求才能对其进行压缩：
 *              feign.compression.request.enabled=true
 *              feign.compression.request.min-types=text/html,application/xml,application/json
 *              feign.compression.request.size=2048
 *              上面的两个参数均为默认值
 * 8、Spring Cloud Zuul
 *      # 面向服务的路由
        zuul.routes.api-a.path=/api-a/*
        zuul.routes.api-a.serviceId=spring-cloud-provider

        zuul.routes.api-b.path=/api-b/*
        zuul.routes.api-b.serviceId=spring-cloud-consumer
 *      请求过滤，Zuul允许开发者在API网关上通过定义过滤器来实现对请求的拦截与过滤，继承ZuulFilter，实现其4个抽象方法
 *          1、filterType()：过滤器类型，它决定过滤器在请求的哪个生命周期中执行。pre：表示在请求路由之前执行。
 *          2、filterOrder()：过滤器的执行顺序。当请求在一个阶段中存在多个过滤器时，需要根据该方法返回的值来依次执行。
 *          3、shouldFilter()：判断该过滤器是否需要执行，这里我们直接返回了true，因此该过滤器对所有请求都会生效。实际应用中我们可以利用该函数来指定过滤器的有效范围。
 *          4、run()：过滤器的具体逻辑，这里我们通过ctx.setSendZuulResponse(false);令zuul过滤该请求，不对其进行路由，也可以通过ctx.setResponseBody(body)对返回的body内容进行编辑等。
 *          需要将AccessFilter注册到容器
 *      1、忽略表达式，如果不希望过滤/hello接口被路由，配置如下：
 *          zuul.ignored-patterns=/ * * /hello/**
 *          zuul.routes.api-a.path=/api-a/**
 *          zuul.routes.api-a.serviceId=hello-service
 *      2、路由前缀：如果希望为网关上的路由规则增加/api前缀，可以在配置文件中增加：zuul.prefix=/api
 *          代理前缀会默认从路径中移除，可以配置:zuul.stripPrefix=false来关闭该移除代理前缀的动作
 *          通过zuul.routes.<route>.strip-prefix=true来对指定路由关闭移除代理前缀的动作
 *      3、本地跳转
 *      4、Cookie与头信息：默认情况下，Spring Cloud Zuul在请求路由时，会过滤掉HTTP请求头信息中的一些敏感信息，防止它们被传递到下游的外部服务器。默认的敏感头信息通过
 *          zuul.sensitive-headers参数定义，包括Cookie、Set-Cookie、Authorization三个属性，解决办法，增加配置：2种方式
 *              zuul.routes.<router>.sensitive-headers=true     方法一：对指定路由开启自定义敏感头
 *              zuul.routes.<router>.sensitive-headers=         方法二：将指定路由的敏感头设置为空
 *      5、过滤器：
 *          a、
 *
 *
 * 10、
 *
 *
 *
 *
 *
 * zookeeper保证CP（强一致性和可靠性），zookeeper在信息leader选举的时候，选举时间很长，选举期间整个zookeeper集群都不可用，这就导致在选举期间注册服务瘫痪。
 *
 */

//@SpringCloudApplication //这个注解 涵盖了下面这三个注解，可以直接只是用下面这一个，说明一个Spring Cloud标准应用应该包含服务发现以及断路器
@SpringBootApplication
@EnableDiscoveryClient //启用注册服务与发现服务
//@EnableCircuitBreaker  //开启熔断机制
@EnableFeignClients //Feign功能包含了Ribbon与Hystrix
public class SpringCloudWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloudWebApplication.class, args);
	}

	@Bean
    @LoadBalanced
	public RestTemplate restTemplate(){
	    return new RestTemplate();
    }

    //修改默认负载均衡算法，会覆盖Spring Cloud Ribbon默认的负载均衡算法
    @Bean
    public IRule randomRule(){
	    return new RandomRule();//随机算法
    }
}
