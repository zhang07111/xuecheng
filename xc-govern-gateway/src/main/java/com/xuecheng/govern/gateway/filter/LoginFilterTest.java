package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@Component
public class LoginFilterTest extends ZuulFilter {

    //过滤器的类型
    @Override
    public String filterType() {
        /**

         pre:请求再被路由请求前

         roting:在路由请求时调用

         post:在rotting和errror 过滤器之后调用

         error:处理请求时发生错误调用

         */

        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        //返回true表示要执行此过滤器
        return true;
    }

    //过滤器的内容
    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        //得到request
        HttpServletRequest request = currentContext.getRequest();
        //的到response
        HttpServletResponse response = currentContext.getResponse();
        //得到Authorization
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)) {
            //拒绝访问
            currentContext.setSendZuulResponse(false);
            //设置响应码
            currentContext.setResponseStatusCode(200);
            //构建响应的信息
            ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
            //转成json
            String jsonString = JSON.toJSONString(responseResult);
            currentContext.setResponseBody(jsonString);
            //设置contentType
            response.setContentType("application/json;charset=utf-8");
            return null;
        }
        return null;
    }

}
