package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface TeachplanMapper {

    //课程计划查询
    TeachplanNode selectList(String courseId);

}
