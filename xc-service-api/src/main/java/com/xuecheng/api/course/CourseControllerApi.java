package com.xuecheng.api.course;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Page;

import java.util.List;

@Api(value = "课程管理接口", description = "课程管理接口,提供课程的增、删、改、查")
public interface CourseControllerApi {

    @ApiOperation("课程计划查询")
    TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("课程计划添加")
    ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("查询我的课程列表")
    QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest);

    @ApiOperation("添加课程")
    ResponseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("添加课程图片")
    ResponseResult addCoursePic(String courseId, String pic);

    @ApiOperation("查询课程图片")
    CoursePic findCoursePic(String courseID);

    @ApiOperation("删除课程图片")
    ResponseResult deleteCoursePic(String courseId);

    @ApiOperation("查询课程信息")
    CourseBase findCourseView(String courseId);

    @ApiOperation("修改课程基本信息")
    ResponseResult updateCourse(String id, CourseBase courseBase);

    @ApiOperation("获取课程营销信息")
    CourseMarket getCourseMarketById(String courseId);

    @ApiOperation("更新课程营销信息")
    ResponseResult updateCourseMarket(String id, CourseMarket courseMarket);

    @ApiOperation("课程视图查询")
    CourseView courseview(String id);

    @ApiOperation("课程预览")
    CoursePublishResult preview(String id);

    @ApiOperation("课程发布")
    CoursePublishResult publish(String id);

    @ApiOperation("保存课程计划与媒资文件关联")
    ResponseResult saveMedia(TeachplanMedia teachplanMedia);
}
