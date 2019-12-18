package com.xuecheng.test.freemarker.controller;


import com.xuecheng.test.freemarker.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RequestMapping("/freemarker")
@Controller
public class FreemarkerController {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/banner")
    public String index_banner(Map<String, Object> map) {
        ResponseEntity<? extends Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", map.getClass());
        Map body = forEntity.getBody();
        map.putAll(body);
        return "index_banner";
    }

    @RequestMapping("/course")
    public String course(Map<String, Object> map) {
        ResponseEntity<? extends Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/findCouerse/297e7c7c62b888f00162b8a7dec20000", map.getClass());
        Map body = forEntity.getBody();
        map.putAll(body);
        return "course";
    }

    //测试1
    @RequestMapping("/test1")
    public String test1(Map<String, Object> map) {
        map.put("name", "测试1111");

        Student stu1 = new Student();
        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);
        stu2.setBirthday(new Date());
        List<Student> friends = new ArrayList<>();
        friends.add(stu1);
        stu2.setFriends(friends);
        stu2.setBestFriend(stu1);
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);
        //向数据模型放数据
        map.put("stus", stus);
        //准备map数据
        HashMap<String, Student> stuMap = new HashMap<>();
        stuMap.put("stu1", stu1);
        stuMap.put("stu2", stu2);
        //向数据模型放数据
        map.put("stu1", stu1);
        //向数据模型放数据
        map.put("stuMap", stuMap);
        int i = 102420;
        map.put("i", i);


        //返回模板位置 基于templates/test1.ftl
        return "test1";
    }

}
