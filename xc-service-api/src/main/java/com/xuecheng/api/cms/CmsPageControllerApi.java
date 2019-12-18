package com.xuecheng.api.cms;


import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value = "cms页面管理接口", description = "cms页面管理接口,提供页面的增删、改、查")
public interface CmsPageControllerApi {

    //页面查询
    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "path", dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页记录", required = true, paramType = "path", dataType = "int")
    })
    QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    //新增页面
    @ApiOperation("添加新的页面")
    CmsPageResult add(CmsPage cmsPage);

    //根据页面id查询
    @ApiOperation("查询页面")
    CmsPage findById(String id);

    //根据id 查询 然后修改页面
    @ApiOperation("修改页面信息")
    CmsPageResult edit(String id, CmsPage cmsPage);

    //根据id 删除页面
    @ApiOperation("删除页面")
    ResponseResult delete(String id);

    //页面发布
    @ApiOperation("页面发布")
    ResponseResult post(String pageId);

    @ApiOperation("保存页面")
    CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("一键发布页面")
    CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
