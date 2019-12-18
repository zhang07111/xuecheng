package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_course.dao.SysDictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysDictionaryService {

    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    //数据字典类型 查询
    public SysDictionary getByType(String type) {
        return sysDictionaryRepository.getSysDictionaryByDTypeIsIn(type);
    }

}
