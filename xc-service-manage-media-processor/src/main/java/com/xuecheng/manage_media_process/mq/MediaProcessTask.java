package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;

    @Value("${xc-service-manage-media.video-location}")
    String serverPath;

    //接收视频处理消息进行视频处理
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}")
    public void receiveMediaProcessTask(String msg) {
        //解析消息内容,得到mediaId
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId = (String) map.get("mediaId");
        //拿 mediaId从数据库查询文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return;
        }
        MediaFile mediaFile = optional.get();
        //文件类型
        String fileType = mediaFile.getFileType();
        if (!fileType.equals("avi")) {
            mediaFile.setFileType("303004");//无需处理
            mediaFileRepository.save(mediaFile);
            return;
        } else {
            //需要处理
            mediaFile.setFileType("303001");//处理中
            mediaFileRepository.save(mediaFile);
        }
        //使用工具类将avi文件转成MP4
        String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4_name = mediaFile.getFileId() + ".mp4";
        String mp4folder_path = serverPath + mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        //进行处理
        String result = mp4VideoUtil.generateMp4();
        if (result == null || !result.equals("success")) {
            //处理失败
            mediaFile.setFileType("303003");//处理失败
            //定义
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            //记录失败原因
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        //将MP4转成m3u8和ts文件
        String mp4_video_path = serverPath + mediaFile.getFilePath() + mp4_name;
        //m3u8 文件名称
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        //m3u8文件所在目录
        String m3u8folder_path = serverPath + mediaFile.getFilePath() + "hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, mp4_video_path, m3u8_name, m3u8folder_path);
        //生成M3U8文件
        String generateM3u8 = hlsVideoUtil.generateM3u8();
        if (generateM3u8 == null || !generateM3u8.equals("success")) {
            //处理失败
            mediaFile.setFileType("303003");//处理失败
            //定义
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            //记录失败原因
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //处理成功
        //获取ts文件列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        mediaFile.setFileType("303002");
        //定义MediaFileProcess_m3u8
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        //设置ts文件列表
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

        //保存fileUrl
        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8_name;
        mediaFile.setFileUrl(fileUrl);
        mediaFileRepository.save(mediaFile);
     }

}
