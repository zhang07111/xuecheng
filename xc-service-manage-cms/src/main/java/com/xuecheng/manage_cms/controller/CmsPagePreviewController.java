package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import freemarker.cache.StringTemplateLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    PageService pageService;

    //页面预览
    @RequestMapping(value = "/cms/preview/{id}", method = RequestMethod.GET)
    public void preview(@PathVariable("id") String id) throws IOException {
        //执行静态化
        String html = pageService.getPageHtml(id);
        //通过response对象将内容输出
        ServletOutputStream outputStream = response.getOutputStream();
        response.setHeader("Content-type","text/html;charset=utf-8");
        outputStream.write(html.getBytes("utf-8"));
    }

}
