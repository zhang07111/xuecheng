package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsConfigRepository cmsConfigRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    /**
     * @param page             页码从1开始
     * @param size             每页的记录数
     * @param queryPageRequest 查询的条件
     * @return
     */
    public QueryResponseResult findPageAll(int page, int size, QueryPageRequest queryPageRequest) {

        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值对象
        CmsPage cmsPage = new CmsPage();

        //判断站点id是否有值
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }

        //判断模板id是否有值
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }

        //判断别名是否有值
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        Example<CmsPage> cmsPageExample = Example.of(cmsPage, exampleMatcher);

        if (page <= 0) {
            page = 1;
        }
        page = page - 1;

        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(cmsPageExample, pageable);
        QueryResult queryResult = new QueryResult();
        queryResult.setList(cmsPages.getContent());//数据
        queryResult.setTotal(cmsPages.getTotalElements());//总记录数
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);

        return queryResponseResult;
    }

    //新增页面
    public CmsPageResult add(CmsPage cmsPage) {

        if (cmsPage == null) {
            //参数为空  抛出异常
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        //根据 pageName、siteId、webPath 去查 查到就说明已经存在
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null) {
            //说明页面已经存在  抛出异常
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //调用dao 新增页面
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
    }

    //根据id查询页面
    public CmsPage findById(String id) {
        //查询页面
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }

    //修改页面
    public CmsPageResult update(String id, CmsPage cmsPage) {
        //根据id查询
        CmsPage cmsPage1 = findById(id);
        //判断是否存在
        if (cmsPage1 != null) {
            //更新 修改的信息
            cmsPage1.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            cmsPage1.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            cmsPage1.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            cmsPage1.setPageName(cmsPage.getPageName());
            //更新访问路径
            cmsPage1.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            cmsPage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            cmsPage1.setDataUrl(cmsPage1.getDataUrl());
            //执行更新
            CmsPage save = cmsPageRepository.save(cmsPage1);
            return new CmsPageResult(CommonCode.SUCCESS, save);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    //删除页面
    public ResponseResult delete(String id) {
        //先查询
        CmsPage cmsPage = findById(id);
        if (cmsPage != null) {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //根据id 查询cmsConfig
    public CmsConfig getConfigById(String id) {

        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()) {
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    //页面静态化方法
    public String getPageHtml(String pageId) {

        //获取数据模型
        Map model = getModelByPageId(pageId);
        if (model == null) {
            //数据模型获取不到
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取模板内容
        String templateContent = getTemplateByPageId(pageId);
        if (templateContent == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行静态化
        String html = generateHtml(model, templateContent);
        return html;
    }

    /**
     * 执行静态化
     *
     * @param map
     * @param templateContent
     * @return
     */
    private String generateHtml(Map map, String templateContent) {
        //创建配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("template", templateContent);
        //向Configuration 设置模板加载器
        configuration.setTemplateLoader(templateLoader);
        //获取模板
        try {
            Template template = configuration.getTemplate("template", "UTF-8");
            //调用APi进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取页面模板信息
     *
     * @param pageId
     * @return
     */
    private String getTemplateByPageId(String pageId) {
        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面的模板ID
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //查询模板信息
        //获取页面的模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件ID
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从GridFs中取模板文件的内容
            //根据文件id 查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));

            //打开一个下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());

            //创建GridFsResource 对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream());
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    /**
     * 获取数据模型
     *
     * @param pageId
     * @return
     */
    private Map getModelByPageId(String pageId) {

        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出页面的dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)) {
            //dataUrl为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate 获取模型数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    //页面发布
    public ResponseResult post(String pageId) {
        //执行页面静态化
        String html = this.getPageHtml(pageId);
        //将页面静态化文件存储到GridFs中
        CmsPage cmsPage = saveHtml(pageId, html);
        //向MQ发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //保存html到GridFs
    private CmsPage saveHtml(String pageId, String htmlContent) {

        //先得到页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }

        //将html文件内容保存到GriFs中
        ObjectId objectId = null;
        try {
            //将html转输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //通过流保存到GriFs中
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    //向mq 发送消息
    private void sendPostPage(String pageId) {
        //先得到页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }
        //创建消息对象
        Map<String, String> msg = new HashMap<>();
        msg.put("pageId", pageId);
        //转成json串
        String jsonString = JSON.toJSONString(msg);
        //发送给mq
        //站点id
        String siteId = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId, jsonString);
    }

    //保存页面 有则更新 没有则添加
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null) {
            //进行更新
            return this.update(cmsPage1.getPageId(), cmsPage);
        }
        return this.add(cmsPage);
    }

    //页面发布
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {

        //将页面信息保存到cms_page中
        CmsPageResult cmsPageResult = this.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面id
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();

        //发布
        ResponseResult responseResult = this.post(pageId);
        if (!responseResult.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //站点ID
        String siteId = cmsPage1.getSiteId();
        CmsSite cmsSite = findCmsSiteById(siteId);
        //拼接url
        String pageUrl = cmsSite.getSiteDomain() + cmsSite.getSiteWebPath() + cmsPage1.getPageWebPath() + cmsPage1.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
    }

    //查询站点信息
    public CmsSite findCmsSiteById(String id) {
        if (id == null) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }
        Optional<CmsSite> optional = cmsSiteRepository.findById(id);
        if (optional.isPresent()) {
            CmsSite cmsSite = optional.get();
            return cmsSite;
        }
        return null;
    }
}
