package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.cms.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CourseService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course‐publish.siteId}")
    private String publish_siteId;
    @Value("${course‐publish.templateId}")
    private String publish_templateId;
    @Value("${course‐publish.previewUrl}")
    private String previewUrl;

    //课程计划查询
    public TeachplanNode findTeachplanList(String courseId) {
        return teachplanMapper.selectList(courseId);
    }

    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan == null || teachplan.getCourseid().isEmpty() || teachplan.getPname().isEmpty()) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }
        //课程计划
        String courseId = teachplan.getCourseid();
        //parentId
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)) {
            //取出该课程的根节点
            parentid = getTeachplanRoot(courseId);
        }
        //取出父节点 查询父节点的级别
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan teachplan1 = optional.orElse(null);
        //父节点级别
        String grade = teachplan1.getGrade();
        //新节点
        Teachplan teachplanNew = new Teachplan();
        //将页面提交的 信息拷贝到新对象中
        BeanUtils.copyProperties(teachplan, teachplanNew);

        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseId);
        if (grade.equals("1")) {
            teachplanNew.setGrade("2");//级别 根据父节点的级别设置
        } else {
            teachplanNew.setGrade("3");
        }
        //保存到数据库中
        teachplanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程的根节点 查不到就创建一个根节点
    private String getTeachplanRoot(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();
        //查询课程的根节点
        List<Teachplan> teachplans = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        //判断有没有根节点
        if (teachplans == null || teachplans.size() <= 0) {
            //创建根节点
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        return teachplans.get(0).getId();
    }

    //我的课程列表分页查询
    public QueryResponseResult<CourseInfo> findCourseList(String company_id, int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        if (page <= 0) {
            page = 1;
        }
        if (size == 0) {
            size = 7;
        }
        courseListRequest.setCompanyId(company_id);
        //分页查询前需要调用 这个
        PageHelper.startPage(page, size);
        Page<CourseInfo> courseInfos = courseMapper.findCourseInfoList(courseListRequest);
        QueryResult queryResult = new QueryResult();
        //把分页数据设置进去
        queryResult.setList(courseInfos.getResult());
        queryResult.setTotal(courseInfos.getTotal());

        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }


    //新增课程
    @Transactional
    public ResponseResult addCourseBase(CourseBase courseBase) {
        //为空 说明提交的参数不对
        if (courseBase == null || courseBase.getName().isEmpty()) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }

        //判断 选择没选择课程分类
        if (courseBase.getMt() == null || courseBase.getMt().equals("") || courseBase.getSt() == null) {
            //没选择就创建
            // 二级分类
            String Mt = this.createTwo(courseBase.getName());
            courseBase.setMt(Mt);
            //三级分类
            String st = this.createthree(Mt);
            courseBase.setSt(st);
        }
//        String id = UUID.randomUUID().toString();
//        courseBase.setId(id);

        courseBaseRepository.save(courseBase);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //创建一个新的二级分类
    private String createTwo(String name) {
        //创建二级分类
        Category category = new Category();
        category.setName(name);
        category.setLabel(name);
        category.setParentid("1");
        category.setIsleaf("0");
        category.setIsshow("1");
        //查询出当前的顺序
        String orderBy = categoryMapper.findOrderBy("1");
        int order = Integer.parseInt(orderBy);
        category.setOrderby(order + 1);
        String id = "1-" + (order + 1);
        category.setId(id);
        Category save = categoryRepository.save(category);
        //返回二级分类的id
        return save.getId();
    }

    //创建一个新的三级分类
    private String createthree(String mt) {
        //创建二级分类
        Category category = new Category();
        category.setName("自行修改");
        category.setLabel("自行修改");
        category.setParentid(mt);
        category.setIsleaf("1");
        category.setIsshow("1");
        //查询出当前的顺序
        String orderBy = categoryMapper.findOrderBy(mt);
        //如果不为空说明不是第一次创建
        if (orderBy != null) {
            int order = Integer.parseInt(orderBy);
            category.setOrderby(order + 1);
            String id = mt + (order + 1);
            category.setId(id);
        }
        //为空就走
        category.setOrderby(1);
        category.setId(mt + "-1");
        Category save = categoryRepository.save(category);
        //返回三级分类的id
        return save.getId();
    }

    //根据课程id 查询信息
    public CourseBase findCourseView(String courseId) {
        //参数为空
        if (courseId == null) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }
        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        CourseBase courseBase = null;
        if (optional.isPresent()) {
            courseBase = optional.get();
        }
        return courseBase;
    }

    //修改课程信息
    @Transactional
    public ResponseResult updateCourse(String id, CourseBase courseBase) {
        if (id == null || courseBase == null) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }
        //先查询有没有这个课程
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()) {
            CourseBase base = optional.get();
            //把更新的数据设置进去
            base.setName(courseBase.getName());
            base.setUsers(courseBase.getUsers());
            base.setMt(courseBase.getMt());
            base.setSt(courseBase.getSt());
            base.setGrade(courseBase.getGrade());
            base.setStudymodel(courseBase.getStudymodel());
            base.setStatus(courseBase.getStatus());
            courseBaseRepository.save(base);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //根据站点Id 查询营销信息
    public CourseMarket getCourseMarketById(String courseId) {
        if (courseId == null || courseId.equals("")) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        CourseMarket courseMarket = null;
        if (optional.isPresent()) {
            courseMarket = optional.get();
            return courseMarket;
        }
        return courseMarket;
    }

    //更新营销信息
    @Transactional
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket) {
        if (id == null || courseMarket == null) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }
        Optional<CourseMarket> op = courseMarketRepository.findById(id);
        CourseMarket courseMarket1 = null;
        if (op.isPresent()) {
            courseMarket1 = op.get();
            courseMarket1.setCharge(courseMarket.getCharge());
            courseMarket1.setValid(courseMarket.getValid());
            courseMarket1.setQq(courseMarket.getQq());
            courseMarket1.setPrice(courseMarket.getPrice());
            courseMarket1.setEndTime(courseMarket.getEndTime());
            courseMarket1.setStartTime(courseMarket.getStartTime());
            courseMarketRepository.save(courseMarket1);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //添加图片到数据库
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic coursePic = null;
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            coursePic = optional.get();
        }
        if (coursePic == null) {
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程图片
    public CoursePic findCoursePic(String courseID) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseID);
        if (optional.isPresent()) {
            CoursePic coursePic = optional.get();
            return coursePic;
        }
        return null;
    }

    //删除图片信息
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {

        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result > 0) {
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //查询课程的视图,包括
    public CourseView getcourseView(String id) {
        CourseView courseView = new CourseView();
        //查询课程基本信息
        CourseBase courseBase = findCourseView(id);
        if (courseBase != null) {
            courseView.setCourseBase(courseBase);
        }
        //查询课程图片
        CoursePic coursePic = findCoursePic(id);
        if (coursePic != null) {
            courseView.setCoursePic(coursePic);
        }
        //查询课程营销信息
        CourseMarket courseMarket = getCourseMarketById(id);
        if (courseMarket != null) {
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程计划
        TeachplanNode teachplanNode = findTeachplanList(id);
        if (courseMarket != null) {
            courseView.setTeachplanNode(teachplanNode);
        }
        return courseView;
    }

    //课程预览
    public CoursePublishResult preview(String id) {
        CourseBase courseBase = this.findCourseView(id);
        //请求cms添加页面
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(id + ".html");
        //页面别名
        cmsPage.setPageAliase(courseBase.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre + id);
        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            //抛出异常
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        //页面Id
        String pageId = cmsPage1.getPageId();
        //拼装页面预览的路径
        String previewUrl = this.previewUrl + pageId;
        //返回
        return new CoursePublishResult(CommonCode.SUCCESS, previewUrl);
    }

    //课程发布
    @Transactional
    public CoursePublishResult publish(String id) {
        CourseBase courseBase = this.findCourseView(id);
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(id + ".html");
        //页面别名
        cmsPage.setPageAliase(courseBase.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre + id);

        //调用cms一键发布
         CmsPostPageResult postPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!postPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        //保存课程的发布状态为"已发布"
        CourseBase base = this.saveCoursePubState(id);
        if (base == null) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }

        //保存课程索引信息
        //先创建一个对象
        CoursePub coursePub = createCoursePub(id);
        //保存
        saveCoursePub(id, coursePub);

        //得到页面的Url
        String pageUrl = postPageResult.getPageUrl();

        //向teachplanMediaPub中保存课程媒资信息
        saveTeachplanMediaPub(id);

        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //向teachplanMediaPub中保存课程媒资信息
    private void saveTeachplanMediaPub(String courseId) {
        //先删除teachplanMediaPub中的数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //从teachplanMedia查询记录
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        //将teachplanMediaList插入teachplanMediaPub
        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        //将从teachplanMedia查询记录放入pub中
        for (TeachplanMedia t :
                teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(t, teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }

    //创建coursePub对象
    private CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);

        //查询course_base
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            //将course_base属性拷贝到CoursePub中
            BeanUtils.copyProperties(courseBase, coursePub);
        }

        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if (picOptional.isPresent()) {
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }

        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()) {
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //将课程计划转成json
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);

        return coursePub;
    }

    //保存coursePub到数据库
    private CoursePub saveCoursePub(String courseId, CoursePub coursePub) {
        //根据课程id查询coursePub
        Optional<CoursePub> optional = coursePubRepository.findById(courseId);
        CoursePub coursePubNew = null;
        if (optional.isPresent()) {
            coursePubNew = optional.get();
        } else {
            coursePubNew = new CoursePub();
        }
        //设置数据
        BeanUtils.copyProperties(coursePub, coursePubNew);
        coursePubNew.setId(courseId);
        //时间戳
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM‐dd HH:mm:ss");
        String format = dateFormat.format(new Date());
        coursePubNew.setPubTime(format);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    //更新课程状态为已发布 202002
    private CourseBase saveCoursePubState(String courseId) {
        CourseBase courseBase = this.findCourseView(courseId);
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);
        return courseBase;
    }

    //保存课程计划与媒资文件的关联信息
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }
        //检验课程是否是3级
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALD_PARAM);
        }
        //查询到了 教学计划
        Teachplan teachplan = optional.get();
        //取出等级
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !grade.equals("3")) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIS_TEACAHPLAN_GRADEERROR);
        }
        //查询teachplanmedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia teachplanMediaNew = null;
        if (mediaOptional.isPresent()) {
            teachplanMediaNew = mediaOptional.get();
        } else {
            teachplanMediaNew = new TeachplanMedia();
        }

        //保存到数据库
        teachplanMediaNew.setCourseId(teachplan.getCourseid());//课程id
        teachplanMediaNew.setMediaId(teachplanMedia.getMediaId());//媒资文件id
        teachplanMediaNew.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件原始名称
        teachplanMediaNew.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件路径
        teachplanMediaNew.setTeachplanId(teachplanId);//id
        teachplanMediaRepository.save(teachplanMediaNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
