package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface CategoryMapper {

    //课程分类查询
    CategoryNode findList();

    //查出当前的orderBy的最大数
    String findOrderBy(String parentid);

}
