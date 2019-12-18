package com.xuecheng.search.service;

import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.course.type}")
    private String type;
    @Value("${xuecheng.course.source_field}")
    private String source_field;
    @Value("${xuecheng.media.index}")
    private String media_index;
    @Value("${xuecheng.media.type}")
    private String media_type;
    @Value("${xuecheng.media.source_field}")
    private String media_source_field;

    @Autowired
    RestHighLevelClient highLevelClient;

    //课程搜索
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        if (courseSearchParam == null) {
            courseSearchParam = new CourseSearchParam();
        }
        //创建搜索的请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置搜索类型
        searchRequest.types(type);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤源字段
        String[] split = source_field.split(",");
        searchSourceBuilder.fetchSource(split, new String[]{});
        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //搜索条件
        //根据关键字搜索
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                    .minimumShouldMatch("70%")
                    .field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            //根据分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            //根据分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            //根据难度等级
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //设置分页参数
        if (page <= 0) {
            page = 1;
        }
        if (page <= 0) {
            size = 12;
        }
        //起始记录下标
        int from = (page - 1) * size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);

        QueryResult<CoursePub> queryResult = new QueryResult<>();
        List<CoursePub> list = new ArrayList<>();
        try {
            //执行搜索
            SearchResponse searchResponse = highLevelClient.search(searchRequest);
            //获取响应结果
            SearchHits hits = searchResponse.getHits();
            //匹配的总记录数
            long totalHits = hits.totalHits;
            queryResult.setTotal(totalHits);
            SearchHit[] hitsHits = hits.getHits();
            for (SearchHit hits1 : hitsHits) {
                CoursePub coursePub = new CoursePub();
                //源文档
                Map<String, Object> sourceAsMap = hits1.getSourceAsMap();
                //取出id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                //取出name
                String name = (String) sourceAsMap.get("name");
                //取出高亮字段
                Map<String, HighlightField> highlightFieldMap = hits1.getHighlightFields();
                if (highlightFieldMap != null) {
                    StringBuffer sb = new StringBuffer();
                    HighlightField highlightField = highlightFieldMap.get("name");
                    if (highlightField != null) {
                        Text[] fragments = highlightField.getFragments();
                        for (Text text : fragments) {
                            sb.append(text);
                        }
                        name = sb.toString();
                    }
                }
                coursePub.setName(name);
                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                try {
                    if (sourceAsMap.get("price") != null) {
                        price = (Double) sourceAsMap.get("price");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice(price);
                Double price_old = null;
                try {
                    if (sourceAsMap.get("price_old") != null) {
                        price_old = (Double) sourceAsMap.get("price_old");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice_old(price_old);

                list.add(coursePub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        queryResult.setList(list);
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);

        return queryResponseResult;
    }

    //使用Es的客户端向es请求查询
    public Map<String, CoursePub> getAll(String id) {
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //指定type
        searchRequest.types(type);

        //定义SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termQuery
        searchSourceBuilder.query(QueryBuilders.termQuery("id", id));

        searchRequest.source(searchSourceBuilder);
        //最终要返回的课程信息
        CoursePub coursePub = new CoursePub();
        Map<String, CoursePub> hashMap = new HashMap<>();
        try {
            SearchResponse search = highLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            SearchHit[] hitss = hits.getHits();
            for (SearchHit searchHit : hitss) {
                //获取源文档内容
                Map<String, Object> map = searchHit.getSourceAsMap();
                String courseId = (String) map.get("id");
                String name = (String) map.get("name");
                String grade = (String) map.get("grade");
                String charge = (String) map.get("charge");
                String pic = (String) map.get("pic");
                String description = (String) map.get("description");
                String teachplan = (String) map.get("teachplan");
                coursePub.setId(courseId);
                coursePub.setCharge(charge);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                hashMap.put(courseId, coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashMap;
    }


    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //指定type
        searchRequest.types(media_type);

        //定义SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termsQuery根据多个id 查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        //过虑源字段
        String[] includes = media_source_field.split(",");
        searchSourceBuilder.fetchSource(includes,new String[]{});
        searchRequest.source(searchSourceBuilder);
        //使用es客户端进行搜索请求Es
        List<TeachplanMediaPub> list = new ArrayList<>();
        long total = 0;
        try {
            //   执行搜索
            SearchResponse search = highLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            total = hits.getTotalHits();
            SearchHit[] hitss = hits.getHits();
            for (SearchHit searchHit : hitss) {
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();

                //取出课程计划媒资信息
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");

                //设置到java Bean中
                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);

                list.add(teachplanMediaPub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //构建返回课程媒资信息对象
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(list);
        queryResult.setTotal(total);
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }
}
