package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //创建jwt令牌
    @Test
    public void testCreateJwt() {
        //密钥库文件
        String keystore = "xc.keystore";
        //密钥库的密码
        String keystore_password = "xuechengkeystore";

        //密钥库文件路径
        ClassPathResource classPathResource = new ClassPathResource(keystore);

        //密钥别名
        String alias = "xckey";
        //密钥

        //密钥的访问密码
        String key_password = "xuecheng";
        //密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource, keystore_password.toCharArray());
        //密钥对(公钥和私钥)
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, key_password.toCharArray());
        //获取私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        //jwt令牌的内容
        Map<String, String> body = new HashMap<>();
        body.put("name", "itcast");
        String jsonString = JSON.toJSONString(body);
        //生成jwt令牌
        Jwt jwt = JwtHelper.encode(jsonString, new RsaSigner(aPrivate));
        //生成jwt令牌编码
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

    //校验jwt令牌
    @Test
    public void testVerify() {
        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        //jwt 令牌
        String jwtString = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6IjEwMTAwMiIsImlkIjoiNDkiLCJleHAiOjE1NzYwNzk2NDYsImF1dGhvcml0aWVzIjpbInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYmFzZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfZGVsIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9saXN0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wbGFuIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZSIsImNvdXJzZV9maW5kX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXIiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX21hcmtldCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcHVibGlzaCIsImNvdXJzZV9maW5kX3BpYyIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYWRkIl0sImp0aSI6IjA2YWU3YzA3LWZhMWYtNGNkNi05MjczLWJmNWY4ODI2YTkxNCIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.ECzboKv_gB8kYwSqZSybwJyTEGbLb9rKS4FcZisB8jNFrGqwM5z2Jwfu_GD6zI0UQ4XUXSSRDPEf7oX2xUqQDRhNlvjCoJ3OBJ66Ix4EeruOeMC4S2Bhz_WVbSGnM-C-8r7lbYOZP4ce51czUpnsMGEyuxHoqW_yTPd8kNToJwGOJ2q-pxbkDhzVPWa1JlP8kWPqwHPg3TLuPYFtHwQIJPM69hh2UvAUL387wNxUjiobuocM8rIAUkezgT7w_sOhZ_NbC7eDyZJuSLRMg0eUwsgN7Qj3aGS8bDEt3281ka5b2xBUU54DENusBUJusswiM51OcsrPYO7cCbNvJHMhtQ";

        //校验jwt令牌
        Jwt jwt = JwtHelper.decodeAndVerify(jwtString, new RsaVerifier(publickey));
        //拿到之前定义的内容
        String claims = jwt.getClaims();
        System.out.println(claims);
    }

    //效验readis
    @Test
    public void testRedis() {
        //定义key
        String key = "c7a46cc1-fedf-4772-ae0d-5d1fa5a7b82a";
        //定义value
        Map<String, String> value = new HashMap<>();
        value.put("jwt", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU3NTU4NTQ5OCwianRpIjoiYzdhNDZjYzEtZmVkZi00NzcyLWFlMGQtNWQxZmE1YTdiODJhIiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.TUQRvI__PM3rtUQtk0Hq1kwJjAao1IL8d_vYXcKhcmwXBtqPmFXLdytJW2swC9XnjHA45Sn1UrdIB93Id6w9ZMCajD8whNJKlYTcXeXc9iBYrU-V4LSEiEdBzy87YrN9H_ffkCTmz8aA41Ca_Dz0inddcG-ySjOgYvHOEPlEgvTQ_Jf3BUkefZT6PC-S_Dt4GBFfA_BjYQpu5XxH6tvrgtPYngjNR6yvmLFnESMtdeby3hE3s5AKzR7RUouTAuNZ3XfmEVHQvgsnYiWklZXNEAkMSzRSRAb1qkt9tjgEY9CurMenqkBVioxDqXqg6tfcvIUxYrTdrj_WfLJQMjm1nQ");
        value.put("refresh_token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJhdGkiOiJjN2E0NmNjMS1mZWRmLTQ3NzItYWUwZC01ZDFmYTVhN2I4MmEiLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU3NTU4NTQ5OCwianRpIjoiMTI3OWZkMmMtMzdmOS00NzRlLTgwMjctZmM2YzQ0ZGI4NDA3IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.RdAClSUGHEbz8EFpMEhWqCN8UUj_qbdzG1C51185eF-_cMmgdCRMzr9we17fOEF82Dc7uyZOfkkONdTFXP-OnuKMFZGzE2UQDHUYHw4uDGKXxo-yQa5s8-IEtda4pwCLdQQKM1ChDUFzx9zTrodjY1p6ziv4-vdQMxOKqCmpbDTZEDhHRgg-iqV3IXBQyICVUgcAFN7tdHIeCwWxUQ88AWk0Giji3RQDVUCO_6rCaD6abapWzm-M8uFNM_FBKOOfdrVmsZZG_5PDZeZXA8hOoBHL40ywpyXPLY_j6BUGJVxk6Zm37fQV5KrLO5KBTjaQbdLv33q_AxwP3xEoS5ZnBA");
        //存储数据
        String jsonString = JSON.toJSONString(value);

        stringRedisTemplate.boundValueOps(key).set(jsonString, 30, TimeUnit.SECONDS);
        //获取数据
        String json = stringRedisTemplate.opsForValue().get(key);
        System.out.println(json);

    }

}
