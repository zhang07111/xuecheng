package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.system.SysDictionary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sound.midi.Soundbank;
import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    @Test
    public void testCourseBaseRepository() {
        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }

    }

    @Test
    public void testCourseMapper() {
        CourseBase courseBase = courseMapper.findCourseBaseById("402885816240d276016240f7e5000002");
        System.out.println(courseBase);
    }

    @Test
    public void testFindTeachplanList() {
        Teachplan teachplan = teachplanMapper.selectList("4028e581617f945f01617f9dabc40000");
        System.out.println(teachplan);
    }

    @Test
    public void testPageHelper() {
        //查询第一页,每一页显示10条记录
        PageHelper.startPage(1, 10);
        Page<CourseBase> courseList = courseMapper.findCourseList();
        List<CourseBase> courseBases = courseList.getResult();
        long total = courseList.getTotal();
        System.out.println(courseBases);
    }

    @Test
    public void testPageHelper2() {
        //查询第一页,每一页显示10条记录
        PageHelper.startPage(1, 7);
        Page<CourseInfo> courseInfoList = courseMapper.findCourseInfos();
        List<CourseInfo> result = courseInfoList.getResult();
        System.out.println(result);
    }

    @Test
    public void testMongoDb() {
        SysDictionary sysDictionaryByDTypeIsIn = sysDictionaryRepository.getSysDictionaryByDTypeIsIn("200");
        System.out.println(sysDictionaryByDTypeIsIn);
    }

}
