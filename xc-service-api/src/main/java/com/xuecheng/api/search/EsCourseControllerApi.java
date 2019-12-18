package com.xuecheng.api.search;

import com.xuecheng.framework.domain.cms.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Api(value = "课程搜索", description = "课程搜索", tags = {"课程搜索"})
public interface EsCourseControllerApi {

    //搜索课程信息
    @ApiOperation("课程综合搜索")
    QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);

    @ApiOperation("根据课程id查询课程信息")
    Map<String, CoursePub> getAll(String id);

    @ApiOperation("根据课程计划id查询课程媒资信息")
    TeachplanMediaPub getmedia(String id);
}
