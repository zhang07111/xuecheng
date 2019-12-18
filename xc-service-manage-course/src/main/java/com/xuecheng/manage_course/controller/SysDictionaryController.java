package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.SysDicthinaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_course.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys")
public class SysDictionaryController implements SysDicthinaryControllerApi {

    @Autowired
    SysDictionaryService sysDictionaryService;

    @Override
    @GetMapping("/dictionary/get/{dtype}")
    public SysDictionary getByType(@PathVariable("dtype") String type) {
        return sysDictionaryService.getByType(type);
    }
}
