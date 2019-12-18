package com.xuecheng.manage_course.client;


import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("xc-service-manage-cms")
public interface CmsPageClient {

    @GetMapping("/cms/page/get/{id}")
    CmsPage findById(@PathVariable("id") String id);


    @PostMapping("/cms/page/save")
    CmsPageResult save(@RequestBody CmsPage cmsPage);

    //一键发布
    @PostMapping("/cms/page/postPageQuick")
    CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage);

}
