package com.yang.rule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RetryRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 针对某一个服务定制自定义的负载均衡策略
 * 注意：这个自定义配置类不能放在@ComponentScan所扫描的包以及子包下面，否则这个自定义配置类会被所有的Ribbon客户端所共享，也就是定义成了全局的了，就达不到特殊化定制的目的了。
 *
 */
@Configuration
public class MySelfRule {

    @Bean
    public IRule retryRule(){
        return new RetryRule();
    }
}
