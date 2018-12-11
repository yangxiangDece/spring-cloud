package com.yang.springcloud.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求过滤，Zuul允许开发者在API网关上通过定义过滤器来实现对请求的拦截与过滤，继承ZuulFilter，实现其4个抽象方法
 *      1、filterType()：过滤器类型，它决定过滤器在请求的哪个生命周期中执行。pre：表示在请求路由之前执行。
 *      2、filterOrder()：过滤器的执行顺序。当请求在一个阶段中存在多个过滤器时，需要根据该方法返回的值来依次执行。
 *      3、shouldFilter()：判断该过滤器是否需要执行，这里我们直接返回了true，因此该过滤器对所有请求都会生效。实际应用中我们可以利用该函数来指定过滤器的有效范围。
 *      4、run()：过滤器的具体逻辑，这里我们通过ctx.setSendZuulResponse(false);令zuul过滤该请求，不对其进行路由，也可以通过ctx.setResponseBody(body)对返回的body内容进行编辑等。
 *
 *     需要将AccessFilter注册到容器
 *
 */
//@Component
public class AccessFilter extends ZuulFilter {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccessFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx=RequestContext.getCurrentContext();
        HttpServletRequest request=ctx.getRequest();

        LOGGER.info("send {} request to {}",request.getMethod(),request.getRequestURL().toString());

        String accessToken = request.getParameter("accessToken");
        if(StringUtils.isBlank(accessToken)){
            LOGGER.warn("access token is empty...");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            return null;
        }
        LOGGER.info("access token ok...");
        return null;
    }
}
