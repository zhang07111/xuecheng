package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaFileService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    //查询我的媒资列表
    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        if (queryMediaFileRequest == null) {
            queryMediaFileRequest = new QueryMediaFileRequest();
        }

        //条件的值对象
        MediaFile mediaFile = new MediaFile();
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())) {
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())) {
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())) {
            mediaFile.setFileType(queryMediaFileRequest.getProcessStatus());
        }

        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains());

        //定义example 条件对象
        Example<MediaFile> example = Example.of(mediaFile, exampleMatcher);
        //定义分页对象
        if (page <= 0) {
            page = 1;
        }
        page = page - 1;
        if (size <= 10) {
            size = 10;
        }
        Pageable pageable = new PageRequest(page, size);
        Page<MediaFile> mediaFiles = mediaFileRepository.findAll(example, pageable);
        //总记录数
        long totalPages = mediaFiles.getTotalElements();
        //数据列表
        List<MediaFile> mediaFileList = mediaFiles.getContent();
        //返回的数据集
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setTotal(totalPages);
        queryResult.setList(mediaFileList);

        QueryResponseResult<MediaFile> QueryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return QueryResponseResult;
    }
}
