package com.xuecheng.search;

import org.apache.lucene.search.TermQuery;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    //搜索全部记录
    @Test
    public void testSearchAll() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式 matchAllQuery搜索全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp"}, new String[]{});
        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //前面设置过滤 应该为空
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);

        }
    }

    //分页查询
    @Test
    public void testSearchPage() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式 matchAllQuery搜索全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置分页参数
        int page = 1;
        //每页的记录数
        int size = 1;
        //算出from
        int from = (page - 1) * size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp"}, new String[]{});
        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //前面设置过滤 应该为空
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);
        }
    }

    //termQuery查询
    @Test
    public void testSearchTermQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式 matchAllQuery搜索全部
        searchSourceBuilder.query(QueryBuilders.termQuery("name", "spring"));
        //设置分页参数
        int page = 1;
        //每页的记录数
        int size = 1;
        //算出from
        int from = (page - 1) * size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp"}, new String[]{});
        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //前面设置过滤 应该为空
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);
        }
    }

    //termQuery 根据id查询
    @Test
    public void testTermQueryById() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] ids = new String[]{"1", "2"};
        //搜索方式 matchAllQuery搜索全部
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id", ids));
        //设置分页参数
        int page = 1;
        //每页的记录数
        int size = 2;
        //算出from
        int from = (page - 1) * size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp"}, new String[]{});
        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //前面设置过滤 应该为空
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);
        }
    }

    //MatchQuery查询
    @Test
    public void testMatchQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式 matchAllQuery搜索全部
        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发框架").minimumShouldMatch("80%"));
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //前面设置过滤 应该为空
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);
        }
    }

    //testMultiMatchQuery
    @Test
    public void testMultiMatchQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式 MultiMatchQuery搜索全部
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring java", "name", "description")
                .minimumShouldMatch("50%").field("name", 10));
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //前面设置过滤 应该为空
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);
        }
    }

    //boolean 查询
    @Test
    public void testbooleanQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式 booleanQuery搜索

        //定义MultiMatcherQuery
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring java", "name", "description")
                .minimumShouldMatch("80%").field("name", 10);
        //定义TermQueryBuilder
        TermQueryBuilder termQuery = QueryBuilders.termQuery("studymodel", "201001");

        //定义boolean查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(matchQueryBuilder);
        boolQueryBuilder.must(termQuery);

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //前面设置过滤 应该为空
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);
        }
    }

    // 过滤器 查询
    @Test
    public void testFilterQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式 booleanQuery搜索

        //定义MultiMatcherQuery
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring java", "name", "description")
                .minimumShouldMatch("80%").field("name", 10);

        //定义boolean查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(matchQueryBuilder);
        //定义过滤器
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //前面设置过滤 应该为空
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);
        }
    }

    // 排序 查询
    @Test
    public void testSort() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //定义boolean查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //定义过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //添加排序
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});
        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //前面设置过滤 应该为空
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);
            System.out.println(price);
        }
    }

    // 高亮显示 查询
    @Test
    public void testHlight() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchResult = new SearchRequest("xc_course");
        //指定类型
        searchResult.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring java", "name", "description")
                .minimumShouldMatch("80%").field("name", 10);

        //定义boolean查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //定义过滤器
        boolQueryBuilder.must(matchQueryBuilder);
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp", "description"}, new String[]{});


        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tar>");
        highlightBuilder.postTags("</tar>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));

        searchSourceBuilder.highlighter(highlightBuilder);

        //向搜索请求设置搜索源
        searchResult.source(searchSourceBuilder);
        //执行搜索
        SearchResponse response = client.search(searchResult);
        //搜索结果
        SearchHits hits = response.getHits();
        //匹配到的总记录数
        long totalHits = hits.totalHits;
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");

            //获取高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightBuilder != null) {
                HighlightField highlightField = highlightFields.get("name");
                Text[] texts = highlightField.getFragments();
                StringBuffer sb = new StringBuffer();
                for (Text t : texts) {
                    sb.append(t);
                }
                name = sb.toString();
            }

            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期时间
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(timestamp);
            System.out.println(price);
        }
    }

}
