package com.xuecheng.manage_course;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class testRibbon {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testRibbon() {
        //ribbon客户端从eureka中获取服务列表
        String serviceId = "XC-SERVICE-MANAGE-CMS";

        ResponseEntity<Map> entity = restTemplate.getForEntity("http://" + serviceId + "/cms/page/get/5a754adf6abb500ad05688d9", Map.class);
        Map map = entity.getBody();
        System.out.println(map);
    }

}
