package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Value("${auth.tokenValiditySeconds}")
    long ttl;


    @Autowired
    RestTemplate restTemplate;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //用户认证来申请令牌
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //请求spring security 令牌
        AuthToken authToken = applyToken(username, password, clientId, clientSecret);
        if (authToken == null) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }

        //将令牌存储到redis
        String jsonString = JSON.toJSONString(authToken);
        boolean isSave = saveToken(authToken.getAccess_token(), jsonString, ttl);
        if (!isSave) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }


    /**
     * 存储到令牌redis
     *
     * @param access_token 令牌
     * @param content      内容
     * @param ttl          过期时间
     * @return
     */
    private boolean saveToken(String access_token, String content, long ttl) {

        String key = "user_token" + access_token;
        //存入redis
        stringRedisTemplate.boundValueOps(key).set(content, ttl, TimeUnit.SECONDS);
        //查询一下
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire > 0;
    }

    //从redis 查询令牌
    public AuthToken getUserToken(String toKen) {
        String key = "user_token" + toKen;
        //令牌信息
        String value = stringRedisTemplate.opsForValue().get(key);
        //转成对象
        try {
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //申请令牌
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        //从eureka中获取认证服务的一个实例的地址
        ServiceInstance choose = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //此地址就是http://ip:port
        URI uri = choose.getUri();
        //令牌的申请地址 http://localhost:40400/auth/oauth/token
        String authUri = uri + "/auth/oauth/token";

        //定义heard
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        //拿到 basic串
        String basic = getHttpBasic(clientId, clientSecret);
        header.add("Authorization", basic);

        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);

        //设置restTemplate远程调用时,对400 401不让报错 正常返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange = restTemplate.exchange(authUri, HttpMethod.POST, httpEntity, Map.class);
        //申请令牌信息
        Map bodyMap = exchange.getBody();

        if (bodyMap == null || bodyMap.get("access_token") == null
                || bodyMap.get("refresh_token") == null
                || bodyMap.get("jti") == null) {
            //解析spring security返回的错误信息
            if (bodyMap != null) {
                String error_description = (String) bodyMap.get("error_description");
                if (error_description != null) {
                    if (error_description.indexOf("UserDetailsService returned null") >= 0) {
                        ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                    } else if (error_description.indexOf("坏的凭证") >= 0) {
                        ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                    }
                }
            }

            return null;
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti"));//用户身份令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));//刷新令牌
        authToken.setJwt_token((String) bodyMap.get("access_token"));//jwt令牌
        return authToken;
    }

    //获取httpbasic的串
    private String getHttpBasic(String clientId, String clientServer) {
        String s = clientId + ":" + clientServer;
        //将串进行编码
        byte[] encode = Base64Utils.encode(s.getBytes());

        return "Basic " + new String(encode);
    }

    //删除redis中的Token
    public boolean delToken(String uid) {
        String key = "user_token" + uid;
        Boolean aBoolean = stringRedisTemplate.delete(key);

        return true;

    }
}
