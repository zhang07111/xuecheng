package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import jdk.management.resource.ResourceRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkery_media_video;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //得到文件所属目录路径
    private String getFileFolderPath(String fileMd5) {
        return upload_location + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
    }

    //得到文件的路径
    private String getFilePath(String fileMd5, String fileExt) {
        return upload_location + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
    }


    //文件上传前的注册,检查文件是否存在
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

        //检查文件在磁盘上是否存在
        //文件所属目录的路径
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //文件的路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        //文件是否存在
        boolean exists = file.exists();

        //检查文件信息在mongoDb中是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (optional.isPresent() && exists) {
            //文件存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //检查文件目录是否存在 不存在就创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //分块检查
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //检查分块文件是否存在
        //得到分块文件的所在目录
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //块文件
        File chunkFile = new File(fileFolderPath + chunk);
        if (chunkFile.exists()) {
            //块文件存在
            return new CheckChunkResult(CommonCode.SUCCESS, true);
        } else {
            return new CheckChunkResult(CommonCode.SUCCESS, false);
        }
    }

    //上传文件
    public ResponseResult uploadchunk(MultipartFile file, String fileMed5, Integer chunk) {
        //检查分块目录
        //得到分块目录
        String fileFolderPath = this.getFileFolderPath(fileMed5);
        File chunkFileFolder = new File(fileFolderPath);
        if (!chunkFileFolder.exists()) {
            //如果不存在则要自动创建
            chunkFileFolder.mkdirs();
        }
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            //得到上传文件的输入流
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(fileFolderPath + chunk));
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //合并文件
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //合并所有分块
        //得到分块文件的属目录
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        File chunkFileFolder = new File(fileFolderPath);
        //分块文件列表
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);

        //创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);

        //执行合并
        File mergeFile = this.mergeFile(fileList, file);
        if (mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        //校验文件
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5) {
            //校验文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //将文件的信息写入mongoDb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        //文件路径保存相对路径
        String filePath1 = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态设置上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);
        //向Mq发送视频处理消息
        sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //发送视频处理消息
    public ResponseResult sendProcessVideoMsg(String mediaId) {
        //查询数据库mediaId
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }

        //构建消息内容
        Map<String, String> map = new HashMap<>();
        map.put("mediaId", mediaId);
        String jsonString = JSON.toJSONString(map);

        //向MQ发送视频处理消息
        rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkery_media_video, jsonString);


        return new ResponseResult(CommonCode.SUCCESS);
    }

    //校验文件
    private boolean checkFileMd5(File mergeFile, String md5) {

        try {
            //创建文件输入流
            FileInputStream inputStream = new FileInputStream(mergeFile);
            //得到文件的MD5
            String md5Hex = DigestUtils.md5Hex(inputStream);

            //和传入的MD5比较
            if (md5.equalsIgnoreCase(md5Hex)) {
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //合并文件
    private File mergeFile(List<File> chunkFileList, File mergeFile) {

        try {
            //如果合并文件已存在  就删除
            if (mergeFile.exists()) {
                mergeFile.delete();
            } else {
                //创建一个新文件
                mergeFile.createNewFile();
            }


            //对块文件进行排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                        return 1;
                    }
                    return -1;
                }
            });

            //创建一个写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            byte[] b = new byte[1024];
            for (File file :
                    chunkFileList) {
                RandomAccessFile raf_read = new RandomAccessFile(file, "r");
                int len = -1;
                while ((len = raf_read.read(b)) != -1) {
                    raf_write.write(b, 0, len);
                }
                raf_read.close();
            }
            raf_write.close();

            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
