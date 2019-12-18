package com.xuecheng.manage_cms.dao;


import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;



    @Test
    public void testFindAll() {
        List<CmsPage> cmsPageList = cmsPageRepository.findAll();
        System.out.println(cmsPageList);
    }

    @Test
    public void testFindPage() {
        Pageable pageable = PageRequest.of(1, 10);
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(pageable);
        System.out.println(cmsPages);
    }

    @Test
    public void testUpdate() {
        //查询对象
        Optional<CmsPage> optional = cmsPageRepository.findById("5abefd525b05aa293098fca6");
        if (optional.isPresent()) {
            //判断是否为空
            CmsPage cmsPage = optional.get();
            cmsPage.setPageName("index2.html");
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);
        }

    }

    @Test
    public void testFindAllByExample() {
        //分页参数
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        //条件值 对象
        CmsPage cmsPage = new CmsPage();
        //要查询 的站点页面
//        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
//        cmsPage.setTemplateId("5abf57965b05aa2ebcfce6d1");
        cmsPage.setPageAliase("详情");//设置别名
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //定义Example
        Example<CmsPage> cmsPageExample = Example.of(cmsPage, exampleMatcher);
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(cmsPageExample, pageable);
        List<CmsPage> cmsPageList = cmsPages.getContent();
        System.out.println(cmsPageList);
    }
}
