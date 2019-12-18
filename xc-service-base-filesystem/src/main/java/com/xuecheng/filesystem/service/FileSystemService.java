package com.xuecheng.filesystem.service;


import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.ClientInfoStatus;
import java.util.Map;

@Service
public class FileSystemService {

    @Autowired
    FileSystemRepository fileSystemRepository;

    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;
    private Map map;

    //上传文件
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata) {
        if (multipartFile == null) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }

        //将文件上传到fastDFS中,得到一个id
        String fileID = fast_upload(multipartFile);
        if (StringUtils.isEmpty(fileID)) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }

        //将文件ID存到mongDB中
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileID);
        fileSystem.setFilePath(fileID);
        fileSystem.setFiletag(filetag);
        fileSystem.setBusinesskey(businesskey);
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        fileSystem.setFileType(multipartFile.getContentType());
        if (StringUtils.isEmpty(metadata)) {
            Map map = JSON.parseObject(metadata, Map.class);
            fileSystem.setMetadata(map);
        }
        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
    }

    //上传文件到fastDFS
    private String fast_upload(MultipartFile multipartFile) {
        //初始化fastDFS的环境
        initFdfsConfig();
        //创建TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        try {
            TrackerServer trackerServer = trackerClient.getConnection();
            //得到storeStorage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建StorageClient1
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            //上传文件
            byte[] bytes = multipartFile.getBytes();
            //得到文件的原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            //得到扩展名
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            String fileId = storageClient1.upload_appender_file1(bytes, ext, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //初始化FastDFS环境
    private void initFdfsConfig() {
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_charset(charset);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
        } catch (Exception e) {
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }

}
